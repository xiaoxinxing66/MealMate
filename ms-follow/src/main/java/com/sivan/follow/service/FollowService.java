package com.sivan.follow.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.sivan.commons.constant.ApiConstant;
import com.sivan.commons.constant.RedisKeyConstant;
import com.sivan.commons.exception.ParameterException;
import com.sivan.commons.model.domain.ResultInfo;
import com.sivan.commons.model.domain.ResultInfoUtil;
import com.sivan.commons.model.pojo.Follow;
import com.sivan.commons.model.vo.ShortDinerInfo;
import com.sivan.commons.model.vo.SignInDinerInfo;
import com.sivan.commons.utils.AssertUtil;
import com.sivan.follow.mapper.FollowMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @Author 不知名网友鑫
 * @Date 2023/9/10
 **/
@Service
public class FollowService {
    @Resource
    private FollowMapper followMapper;
    @Value("${service.name.ms-oauth-server}")
    private String oauthServerName;
    @Value("${service.name.ms-feeds-server}")
    private String feedsServerName;
    @Resource
    private RestTemplate restTemplate;
    @Resource
    private RedisTemplate redisTemplate;
    @Value("${service.name.ms-diners-server}")
    private String dinersServerName;

    public Set<Integer> findFollowers(Integer dinerId){
        AssertUtil.isNotNull(dinerId , "请选择要查看的用户");
        Set<Integer> followers = redisTemplate.opsForSet().members(RedisKeyConstant.followers.getKey() + dinerId);
        return  followers;
    }

    /**
     *
     * @param followDinerId 关注的食客Id
     * @param isFollowed 当前执行的操作 关注 / 取关
     * @param accessToken 根据access_token获取当前操作的用户
     * @param path
     * @return
     */
    public ResultInfo follow(Integer followDinerId,int isFollowed,String accessToken,String path){
        //是否选择关注对象
        AssertUtil.isTrue(followDinerId == null || followDinerId < 1 , "请选择要关注的人");
        //通过access_token获取登录信息 【从我们的授权认证中心获取】【封装方法】
        SignInDinerInfo dinerInfo = loadSignInDinerInfo(accessToken);
        //获取需要关注的用户的信息
        Follow follow = followMapper.selectFollow(dinerInfo.getId(), followDinerId);
        //1. 没有关注信息 && 关注操作 ——>添加关注
        if(follow == null && isFollowed == 1){
            //添加关注
            int count = followMapper.save(dinerInfo.getId(), followDinerId);
            //添加关注列表到Redis
            if(count == 1){
                addToRedisSet(dinerInfo.getId(),followDinerId);
                // 取关 Feed
                sendSaveOrRemoveFeed(followDinerId, accessToken, 1);
            }
            return ResultInfoUtil.build(ApiConstant.SUCCESS_CODE ,"关注成功",path,"关注成功");

        }
            //2. 有关注信息 && 关注状态 && 取关操作 ——>取关
        if(follow != null && isFollowed == 0 && follow.getIsValid() == 1){
            int row = followMapper.update(follow.getId(), isFollowed);
            // 移除Redis关注列表
            if(row == 1){
                removeFromRedisSet(dinerInfo.getId(),followDinerId);
                // 取关 Feed
                sendSaveOrRemoveFeed(followDinerId, accessToken, 0);
            }
            return ResultInfoUtil.build(ApiConstant.SUCCESS_CODE,
                    "成功取关", path, "成功取关");
        }
            //3. 有关注信息 && 取关状态 && 关注操作 ——> 重新关注
        if (follow != null && follow.getIsValid() == 0 && isFollowed == 1) {
            // 重新关注
            int count = followMapper.update(follow.getId(), isFollowed);
            // 添加关注列表到 Redis
            if (count == 1) {
                addToRedisSet(dinerInfo.getId(), followDinerId);
                 // 添加 Feed
                sendSaveOrRemoveFeed(followDinerId, accessToken, 1);
            }
            return ResultInfoUtil.build(ApiConstant.SUCCESS_CODE,
                    "关注成功", path, "关注成功");
        }
        return ResultInfoUtil.buildSuccess(path, "操作成功");
    }
    /**
     * 共同关注列表
     *
     * @param dinerId
     * @param accessToken
     * @param path
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public ResultInfo findCommonsFriends(Integer dinerId, String accessToken, String path) {
        // 是否选择了查看对象
        AssertUtil.isTrue(dinerId == null || dinerId < 1,
                "请选择要查看的人");
        // 获取登录用户信息
        SignInDinerInfo dinerInfo = loadSignInDinerInfo(accessToken);
        // 获取登录用户的关注信息
        String loginDinerKey = RedisKeyConstant.following.getKey() + dinerInfo.getId();
        // 获取登录用户查看对象的关注信息
        String dinerKey = RedisKeyConstant.following.getKey() + dinerId;
        // 计算交集
        Set<Integer> dinerIds = redisTemplate.opsForSet().intersect(loginDinerKey, dinerKey);
        // 没有
        if (dinerIds == null || dinerIds.isEmpty()) {
            return ResultInfoUtil.buildSuccess(path, new ArrayList<ShortDinerInfo>());
        }
        // 调用食客服务根据 ids 查询食客信息
        ResultInfo resultInfo = restTemplate.getForObject(dinersServerName + "findByIds?access_token={accessToken}&ids={ids}",
                ResultInfo.class, accessToken, StrUtil.join(",", dinerIds));
        if (resultInfo.getCode() != ApiConstant.SUCCESS_CODE) {
            resultInfo.setPath(path);
            return resultInfo;
        }
        // 处理结果集
        List<LinkedHashMap> dinnerInfoMaps = (ArrayList) resultInfo.getData();
        List<ShortDinerInfo> dinerInfos = dinnerInfoMaps.stream()
                .map(diner -> BeanUtil.fillBeanWithMap(diner, new ShortDinerInfo(), true))
                .collect(Collectors.toList());

        return ResultInfoUtil.buildSuccess(path, dinerInfos);
    }

//    private void sendSaveOrRemoveFeed(Integer followDinerId, String accessToken, int i) {
//
//    }
    /**
     * 发送请求添加或者移除关注人的Feed列表
     *
     * @param followDinerId 关注好友的ID
     * @param accessToken   当前登录用户token
     * @param type          0=取关 1=关注
     */
    private void sendSaveOrRemoveFeed(Integer followDinerId, String accessToken, int type) {
        String feedsUpdateUrl = feedsServerName + "updateFollowingFeeds/"
                + followDinerId + "?access_token=" + accessToken;
        // 构建请求头
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        // 构建请求体（请求参数）
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("type", type);
        HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(body, headers);
        restTemplate.postForEntity(feedsUpdateUrl, entity, ResultInfo.class);
    }

    /**
     * 从Redis中移除关注
     * @param id
     * @param followDinerId
     */
    private void removeFromRedisSet(Integer id, Integer followDinerId) {
        redisTemplate.opsForSet().remove(RedisKeyConstant.following.getKey() + id,followDinerId);
        redisTemplate.opsForSet().remove(RedisKeyConstant.followers.getKey() + followDinerId,id);
    }

    /**
     * 添加关注信息到Redis
     * @param id
     * @param followDinerId
     */
    private void addToRedisSet(Integer id, Integer followDinerId) {
        // 关注集合
        redisTemplate.opsForSet().add(RedisKeyConstant.following.getKey() + id,followDinerId);
        redisTemplate.opsForSet().add(RedisKeyConstant.followers.getKey() + followDinerId,id);
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
