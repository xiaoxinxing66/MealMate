package com.sivan.diners.controller;

import com.sivan.commons.model.domain.ResultInfo;
import com.sivan.commons.model.domain.ResultInfoUtil;
import com.sivan.diners.service.SignService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * 签到控制层
 */
@RestController
@RequestMapping("sign")
public class SignController {

    @Resource
    private SignService signService;
    @Resource
    private HttpServletRequest request;
    
    /**
     * 签到，可以补签
     *
     * @param access_token
     * @param date
     * @return
     */
    @PostMapping
    public ResultInfo sign(String access_token,
                           @RequestParam(required = false) String date) {
        int count = signService.doSign(access_token, date);
        return ResultInfoUtil.buildSuccess(request.getServletPath(), count);
    }
    /**
     * 获取签到次数 默认当月
     *
     * @param access_token
     * @param date
     * @return
     */
    @GetMapping("count")
    public ResultInfo getSignCount(String access_token, String date) {
        Long count = signService.getSignCount(access_token, date);
        return ResultInfoUtil.buildSuccess(request.getServletPath(), count);
    }
    /**
     * 获取用户签到情况 默认当月
     *
     * @param access_token
     * @param dateStr
     * @return
     */
    @GetMapping
    public ResultInfo getSignInfo(String access_token, String dateStr) {
        Map<String, Boolean> map = signService.getSignInfo(access_token, dateStr);
        return ResultInfoUtil.buildSuccess(request.getServletPath(), map);
    }

}