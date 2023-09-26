package com.sivan.feeds.controller;

import com.sivan.commons.model.domain.ResultInfo;
import com.sivan.commons.model.domain.ResultInfoUtil;
import com.sivan.commons.model.pojo.Feeds;
import com.sivan.commons.model.vo.FeedsVO;
import com.sivan.feeds.service.FeedsService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @Author 不知名网友鑫
 * @Date 2023/9/11
 **/
@RestController
public class FeedsController {
    @Resource
    private FeedsService feedsService;
    @Resource
    private HttpServletRequest request;

    /**
     * 添加 Feed
     *
     * @param feeds
     * @param access_token
     * @return
     */
    @PostMapping
    public ResultInfo<String> create(@RequestBody Feeds feeds, String access_token) {
        feedsService.create(feeds, access_token);
        return ResultInfoUtil.buildSuccess(request.getServletPath(), "添加成功");
    }
    /**
     * 删除 Feed
     *
     * @param id
     * @param access_token
     * @return
     */
    @DeleteMapping("{id}")
    public ResultInfo delete(@PathVariable Integer id, String access_token) {
        feedsService.delete(id, access_token);
        return ResultInfoUtil.buildSuccess(request.getServletPath(), "删除成功");
    }
    /**
     * 变更 Feed
     *
     * @return
     */
    @PostMapping("updateFollowingFeeds/{followingDinerId}")
    public ResultInfo addFollowingFeeds(@PathVariable Integer followingDinerId,
                                        String access_token, @RequestParam int type) {
        feedsService.addFollowingFeed(followingDinerId, access_token, type);
        return ResultInfoUtil.buildSuccess(request.getServletPath(), "操作成功");
    }
    /**
     * 分页获取关注的 Feed 数据
     *
     * @param page
     * @param access_token
     * @return
     */
    @GetMapping("{page}")
    public ResultInfo selectForPage(@PathVariable Integer page, String access_token) {
        List<FeedsVO> feedsVOS = feedsService.selectForPage(page, access_token);
        return ResultInfoUtil.buildSuccess(request.getServletPath(), feedsVOS);
    }
}
