package cn.iselab.inventory.site.web.logic.impl;

import cn.iselab.inventory.site.model.UserOperation;
import cn.iselab.inventory.site.service.UserOperationService;
import cn.iselab.inventory.site.service.UserService;
import cn.iselab.inventory.site.web.data.UserOperationVO;
import cn.iselab.inventory.site.web.data.wrapper.UserOperationVOWrapper;
import cn.iselab.inventory.site.web.logic.BaseLogic;
import cn.iselab.inventory.site.web.logic.UserOperationLogic;
import cn.iselab.inventory.site.web.util.HttpUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.xml.ws.soap.Addressing;
import java.sql.Timestamp;

/**
 * @Author ROKG
 * @Description
 * @Date: Created in 下午4:33 2017/12/5
 * @Modified By:
 */
@Service
public class UserOperationLogicImpl extends BaseLogic implements UserOperationLogic {

    @Autowired
    UserOperationService userOperationService;

    @Autowired
    UserOperationVOWrapper operationVOWrapper;

    @Autowired
    UserService userService;

    @Override
    public void recordUserOperation(HttpServletRequest request,Long userId,String operation){
        String ip = HttpUtil.getIpByRequest(request);
        if("101.37.78.167".equals(ip)) {
            return;
        }
        UserOperation userOperation = new UserOperation();
        userOperation.setIp(ip);
        userOperation.setUserId(userId);
        userOperation.setOperation(operation);
        Timestamp current=new Timestamp(System.currentTimeMillis());
        userOperation.setCreateTime(current);
        userOperationService.create(userOperation);
        LOG.info(String.format("User[%d] Login at [%s]",userId,current.toString()));
    }

    @Override
    public Page<UserOperationVO> getOperationList(Pageable pageable, String keyword){
         Page<UserOperation> operations=userOperationService.getUserOperations(keyword,pageable);
         return operations.map(new Converter<UserOperation, UserOperationVO>() {
             @Override
             public UserOperationVO convert(UserOperation userOperation) {
                 UserOperationVO vo= operationVOWrapper.wrap(userOperation);
                 if(userOperation.getUserId()!=0)
                    vo.setName(userService.getUser(userOperation.getUserId()).getName());
                 return vo;
             }
         });
    }
}
