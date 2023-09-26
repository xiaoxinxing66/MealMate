package com.sivan.points.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.google.common.collect.Lists;

import com.sivan.commons.constant.ApiConstant;
import com.sivan.commons.constant.RedisKeyConstant;
import com.sivan.commons.exception.ParameterException;
import com.sivan.commons.model.domain.ResultInfo;
import com.sivan.commons.model.pojo.DinerPoints;
import com.sivan.commons.model.vo.DinerPointsRankVO;
import com.sivan.commons.model.vo.ShortDinerInfo;
import com.sivan.commons.model.vo.SignInDinerInfo;
import com.sivan.commons.utils.AssertUtil;
import com.sivan.points.mapper.DinerPointsMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.util.*;

/**
 * 积分业务逻辑层
 */
@Service
public class DinerPointsService {

    @Resource
    private DinerPointsMapper dinerPointsMapper;
    @Resource
    private RestTemplate restTemplate;
    @Resource
    private RedisTemplate redisTemplate;
    @Value("${service.name.ms-oauth-server}")
    private String oauthServerName;
    @Value("${service.name.ms-diners-server}")
    private String dinersServerName;
    public static final int TOPN = 20;
  



    /**
     * 添加积分
     *
     * @param dinerId 食客ID
     * @param points  积分
     * @param types   类型 0=签到，1=关注好友，2=添加Feed，3=添加商户评论
     */
    @Transactional(rollbackFor = Exception.class)
    public void addPoints(Integer dinerId, Integer points, Integer types) {
        // 基本参数校验
        AssertUtil.isTrue(dinerId == null || dinerId < 1, "食客不能为空");
        AssertUtil.isTrue(points == null || points < 1, "积分不能为空");
        AssertUtil.isTrue(types == null, "请选择对应的积分类型");

        // 插入数据库
        DinerPoints dinerPoints = new DinerPoints();
        dinerPoints.setFkDinerId(dinerId);
        dinerPoints.setPoints(points);
        dinerPoints.setTypes(types);
        dinerPointsMapper.save(dinerPoints);

        // 将积分保存到 Redis
        redisTemplate.opsForZSet().incrementScore(
                RedisKeyConstant.diner_points.getKey(), dinerId, points);
    }
    /**
     * 查询前 20 积分排行榜，并显示个人排名 -- MySQL
     *
     * @param accessToken
     * @return
     */
    public List<DinerPointsRankVO> findDinerPointRank(String accessToken) {
        // 获取登录用户信息
        SignInDinerInfo signInDinerInfo = loadSignInDinerInfo(accessToken);
        // 统计积分排行榜
        List<DinerPointsRankVO> ranks = dinerPointsMapper.findTopN(TOPN);
        if (ranks == null || ranks.isEmpty()) {
            return Lists.newArrayList();
        }
        // 根据 key：食客 ID value：积分信息 构建一个 Map
        Map<Integer, DinerPointsRankVO> ranksMap = new LinkedHashMap<>();
        for (int i = 0; i < ranks.size(); i++) {
            ranksMap.put(ranks.get(i).getId(), ranks.get(i));
        }
        // 判断个人是否在 ranks 中，如果在，添加标记直接返回
        if (ranksMap.containsKey(signInDinerInfo.getId())) {
            DinerPointsRankVO myRank = ranksMap.get(signInDinerInfo.getId());
            myRank.setIsMe(1);
            return Lists.newArrayList(ranksMap.values());
        }
        // 如果不在 ranks 中，获取个人排名追加在最后
        DinerPointsRankVO myRank = dinerPointsMapper.findDinerRank(signInDinerInfo.getId());
        myRank.setIsMe(1);
        ranks.add(myRank);
        return ranks;
    }
    /**
     * 查询前 20 积分排行榜，并显示个人排名 -- Redis
     *
     * @param accessToken
     * @return
     */
    public List<DinerPointsRankVO> findDinerPointRankFromRedis(String accessToken) {
        // 获取登录用户信息
        SignInDinerInfo signInDinerInfo = loadSignInDinerInfo(accessToken);
        // 统计积分排行榜
//        List<DinerPointsRankVO> ranks = dinerPointsMapper.findTopN(TOPN);
        Set<ZSetOperations.TypedTuple<Integer>> rangeWithScores = redisTemplate.opsForZSet().reverseRangeWithScores(
                RedisKeyConstant.diner_points.getKey(), 0, 19);
        if (rangeWithScores == null || rangeWithScores.isEmpty()) {
            return Lists.newArrayList();
        }
        // 构建集合，拿到用户信息
        List<Integer> rankDinerIds = Lists.newArrayList();
        // 根据 key：食客 ID value：积分信息 构建一个 Map
        Map<Integer, DinerPointsRankVO> ranksMap = new LinkedHashMap<>();
        //初始化排名 ,自己维护一个排名
        int rank = 1;
        for (ZSetOperations.TypedTuple<Integer> rangeWithScore : rangeWithScores) {
            Integer dinerId = rangeWithScore.getValue();
            int points = rangeWithScore.getValue().intValue();
            // 食客id添加到集合中，方便远程发送请求，查询食客信息。
            rankDinerIds.add(dinerId);
            DinerPointsRankVO dinerPointsRankVO = new DinerPointsRankVO();
            dinerPointsRankVO.setId(dinerId);
            // 排名
            dinerPointsRankVO.setRanks(rank);
            // 总积分
            dinerPointsRankVO.setTotal(points);
            // 将Vo对象放入Map中
            ranksMap.put(dinerId,dinerPointsRankVO);
            rank ++;
        }
        ResultInfo resultInfo = restTemplate.getForObject(dinersServerName + "findByIds?access_token=${accessToken}&ids=${ids}",
                ResultInfo.class, accessToken, StrUtil.join(",", rankDinerIds));
        if(resultInfo.getCode() != ApiConstant.SUCCESS_CODE){
            throw new ParameterException(resultInfo.getCode() , resultInfo.getMessage());
        }
        List<LinkedHashMap> dinerInfoMaps = (List<LinkedHashMap>) resultInfo.getData();
        // 完善食客昵称、头像
        for (LinkedHashMap dinerInfoMap : dinerInfoMaps) {
            ShortDinerInfo shortDinerInfo = BeanUtil.fillBeanWithMap(dinerInfoMap,
                    new ShortDinerInfo(), false);
            DinerPointsRankVO rankVo = ranksMap.get(shortDinerInfo.getId());
            rankVo.setNickname(shortDinerInfo.getNickname());
            rankVo.setAvatarUrl(shortDinerInfo.getAvatarUrl());
        }
        // 判断个人是否在 ranks 中，如果在，添加标记直接返回
        if (ranksMap.containsKey(signInDinerInfo.getId())) {
            DinerPointsRankVO myRank = ranksMap.get(signInDinerInfo.getId());
            myRank.setIsMe(1);
            return Lists.newArrayList(ranksMap.values());
        }
        // 如果不在 ranks 中，获取个人排名追加在最后
        Long myrank = redisTemplate.opsForZSet().reverseRank(RedisKeyConstant.diner_points.getKey(), signInDinerInfo.getId());
        if(myrank != null){
            DinerPointsRankVO me = new DinerPointsRankVO();
            BeanUtil.copyProperties(signInDinerInfo , me);
            me.setRanks(myrank.intValue() + 1);
            me.setIsMe(1);
            Double points = redisTemplate.opsForZSet().score(RedisKeyConstant.diner_points.getKey(), signInDinerInfo.getId());
            me.setTotal(points.intValue());
            //追加在最后
            ranksMap.put(signInDinerInfo.getId() , me);
        }
        return Lists.newArrayList(ranksMap.values());
    }
    private SignInDinerInfo loadSignInDinerInfo(String accessToken) {
        // 必须登录
        AssertUtil.mustLogin(accessToken);
        String url = oauthServerName + "user/me?access_token={accessToken}";
        ResultInfo resultInfo = restTemplate.getForObject(url, ResultInfo.class, accessToken);
        if (resultInfo.getCode() != ApiConstant.SUCCESS_CODE) {
            throw new ParameterException(resultInfo.getCode(), resultInfo.getMessage());
        }
        SignInDinerInfo dinerInfo = BeanUtil.fillBeanWithMap((LinkedHashMap) resultInfo.getData(),
                new SignInDinerInfo(), false);
        if (dinerInfo == null) {
            throw new ParameterException(ApiConstant.NO_LOGIN_CODE, ApiConstant.NO_LOGIN_MESSAGE);
        }
        return dinerInfo;
    }

}