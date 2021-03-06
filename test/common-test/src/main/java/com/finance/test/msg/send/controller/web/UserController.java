package com.finance.test.msg.send.controller.web;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.finance.test.msg.send.controller.dto.UserDto;
import com.finance.test.msg.send.facade.Request.UserReqDto;
import com.finance.test.msg.send.facade.response.UserRespDto;
import com.finance.test.msg.send.manager.UserManager;
import com.finance.test.msg.send.model.UserInfo;
import com.finance.test.msg.send.model.UserInfoExample;
import com.finance.test.msg.send.service.UserService;
import com.finance.test.msg.send.util.enums.TestBizCode;
import com.finance.test.msg.send.util.exception.ServiceException;
import com.finance.test.msg.send.util.model.Response;
import com.finance.test.msg.send.util.util.GeneratorUtil;
import lombok.extern.slf4j.Slf4j;
import org.dozer.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 描述：
 * <p>
 * #
 * </p>
 * User: guanhc Date: 2017/6/7 ProjectName:test Version:
 */
@Controller
@Slf4j
@RequestMapping(value = "user/")
public class UserController extends AbstractController {

    @Autowired
    private UserManager userManager;
    @Autowired
    private Mapper dozerMapper;
    @Autowired
    private UserService userService;

    /**
     * 用户登录
     *
     * @param userDto
     */
    @RequestMapping("login")
    public void login(@ModelAttribute("userDto") UserDto userDto) {
        log.info("call UserController.login userDto:{}", userDto);
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        HttpServletResponse response = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getResponse();
        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");
        try {
            UserInfoExample example = new UserInfoExample();
            example.createCriteria().andUserNameEqualTo(userDto.getUserName()).andPasswordEqualTo(userDto.getPassword());
            List<UserInfo> userInfoList = userManager.queryUserInfo(example);
            if (null != userInfoList && userInfoList.size() > 0) {
                request.getSession().setAttribute("token", GeneratorUtil.generatorToken());
                request.getRequestDispatcher("/index.jsp").forward(request, response);
            }
        } catch (ServiceException se) {
            log.error("call UserController.login se:{}", se);
            try {
                response.getWriter().write("<h>登录名或密码错误</h>");
            } catch (IOException io) {
                log.error("call UserController.login error: {}", io);
            }
        } catch (Exception e) {
            log.error("call UserController.login error: {}", e);
        }
    }

    /**
     * 注册
     *
     * @param userDto
     * @return
     */
    @RequestMapping("regist")
    public String registUser(@ModelAttribute("userDto") UserDto userDto) {
        log.info("call UserController.resgit,userDto:{}", userDto);
        HttpServletResponse response = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getResponse();
        Response<UserRespDto> resp = new Response<UserRespDto>();
        try {
            //方法一：
//            String data = JSON.toJSON(userDto).toString();
//            UserReqDto userReqDto = (UserReqDto) JSON.parseObject(data, UserReqDto.class);
            //dozer转换对象报空指针（配置文件spring-dozer配置了空的mapping，已删除）
            UserReqDto userReqDto = dozerMapper.map(userDto, UserReqDto.class);
            resp = userService.regist(userReqDto);
            response.setContentType("text/html;charset=UTF-8");
            response.getWriter().write("<h>注册成功</h>");
        } catch (ServiceException se) {
            log.error("call UserController.regist se:{}", se);
            return responseJson(se);
        } catch (Exception e) {
            return responseJson();
        }
        List<Object> list = new ArrayList<Object>();
        list.add(resp.getResult());
        return responseJson(list, resp.isSuccess(), TestBizCode.BIZ_CODE_200001.getBizCode(), TestBizCode.BIZ_CODE_200001.getBizMsg());
    }

    /**
     * 查询用户信息
     *
     * @param userName
     * @return
     */
    @RequestMapping("queryUserInfo")
    public String queryUserInfo(String userName) {
        log.info("call UserController.queryUserINfo,userName:{}", userName);
        Response<UserRespDto> resp = null;
        try {
            resp = userService.queryUserInfo(userName);
            if (null == resp) {
                List<Object> list = new ArrayList<Object>();
                list.add(resp.getResult());
                return responseJson(list, resp.isSuccess(), TestBizCode.BIZ_CODE_200001.getBizCode(), TestBizCode.BIZ_CODE_200001.getBizMsg());
            }
        } catch (ServiceException se) {
            log.error("call UserController.queryUserINfo,se:{}", se);
            responseJson(se);
        } catch (Exception e) {
            log.error("call UserController.queryUserINfo,e:{}", e);
            responseJson();
        }
        List<Object> list = new ArrayList<Object>();
        list.add(resp.getResult());
        return responseJson(list, false, TestBizCode.BIZ_CODE_200001.getBizCode(), TestBizCode.BIZ_CODE_200001.getBizMsg());
    }


    /**
     * 注销操作
     *
     * @return
     */
    @RequestMapping("logout")
    public String logout(String token) {
        log.info("call UserController.logout,token:{}", token);
        Boolean isSuccess = false;
        try {
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
            HttpServletResponse response = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getResponse();
            HttpSession session = null;
            String sessionToken = (String) request.getSession().getAttribute("token");
            if (null == sessionToken) {
                //防止创建session
                session = request.getSession(false);
                if (null == session) {
                    response.sendRedirect("index.jsp");
                }
            }
            session.removeAttribute("token");
            response.sendRedirect("index.jsp");
            isSuccess = true;
        } catch (ServiceException se) {
            log.error("call UserController.logout,se:{}", se);
            responseJson(se);
        } catch (Exception e) {
            log.error("call UserController.logout,e:{}", e);
            responseJson();
        }
        return responseJson(null, isSuccess, TestBizCode.BIZ_CODE_200001.getBizCode(), TestBizCode.BIZ_CODE_200001.getBizMsg());
    }

    /**
     * 更新用户
     *
     * @return
     */
    @RequestMapping("updateUserInfo")
    public String updateUserInfo(@ModelAttribute("userDto") UserDto userDto) {
        log.info("call UserController.updateUserInfo,userDto:{}", userDto);
        Response<String> resp = null;
        try {
            UserReqDto userReqDto = dozerMapper.map(userDto, UserReqDto.class);
            resp = userService.updateUserInfo(userReqDto);
        } catch (ServiceException se) {
            log.error("call UserController.updateUserInfo,se:{}", se);
            return responseJson(se);
        } catch (Exception e) {
            log.error("call UserController.updateUserInfo,e:{}", e);
            responseJson();
        }
        return responseJson(null, resp.isSuccess(), TestBizCode.BIZ_CODE_200001.getBizCode(), TestBizCode.BIZ_CODE_200001.getBizMsg());
    }
}
