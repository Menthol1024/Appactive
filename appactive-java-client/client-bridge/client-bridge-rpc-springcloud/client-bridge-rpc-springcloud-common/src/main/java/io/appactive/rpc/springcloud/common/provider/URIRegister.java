package io.appactive.rpc.springcloud.common.provider;

import com.alibaba.fastjson.JSON;
import io.appactive.java.api.base.constants.ResourceActiveType;
import io.appactive.rpc.springcloud.common.ServiceMeta;
import io.appactive.rpc.springcloud.common.ServiceMetaObject;
import io.appactive.support.lang.CollectionUtils;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.servlet.Filter;
import java.nio.charset.StandardCharsets;
import java.util.*;


/**
 * @author mageekchiu
 */
@Component
public class URIRegister {

    @Autowired
    private List<FilterRegistrationBean> beanList;

    private ServiceMetaObject serviceMetaObject;

    private final String matchAll = "/**";

    @PostConstruct
    public void doRegister(){
        doRegisterUris();
    }

    /**
     * publish to registry meta like dubbo
     * rather than building a new appactive rule type
     */
    public void doRegisterUris(){
        if (CollectionUtils.isNotEmpty(beanList)){
            List<ServiceMeta> serviceMetaList = new LinkedList<>();
            boolean hasWildChar = false;
            for (FilterRegistrationBean filterRegistrationBean : beanList) {
                Filter filter = filterRegistrationBean.getFilter();
                if (filter==null){
                    continue;
                }
                if (filter instanceof UnitServiceFilter){
                    Collection<String> urlPatterns = filterRegistrationBean.getUrlPatterns();
                    for (String urlPattern : urlPatterns) {
                        if (matchAll.equalsIgnoreCase(urlPattern)){
                            hasWildChar = true;
                        }
                        ServiceMeta serviceMeta = new ServiceMeta(urlPattern, ResourceActiveType.UNIT_RESOURCE_TYPE);
                        serviceMetaList.add(serviceMeta);
                    }
                }else if(filter instanceof CenterServiceFilter){
                    Collection<String> urlPatterns = filterRegistrationBean.getUrlPatterns();
                    for (String urlPattern : urlPatterns) {
                        if (matchAll.equalsIgnoreCase(urlPattern)){
                            hasWildChar = true;
                        }
                        ServiceMeta serviceMeta = new ServiceMeta(urlPattern, ResourceActiveType.CENTER_RESOURCE_TYPE);
                        serviceMetaList.add(serviceMeta);
                    }
                }else if (filter instanceof  NormalServiceFilter){
                    Collection<String> urlPatterns = filterRegistrationBean.getUrlPatterns();
                    for (String urlPattern : urlPatterns) {
                        if (matchAll.equalsIgnoreCase(urlPattern)){
                            hasWildChar = true;
                        }
                        ServiceMeta serviceMeta = new ServiceMeta(urlPattern, ResourceActiveType.NORMAL_RESOURCE_TYPE);
                        serviceMetaList.add(serviceMeta);
                    }
                }
            }
            if (!hasWildChar){
                // 保证所有 service(app+uri) 都纳入管理，不然不好做缓存管理
                ServiceMeta serviceMeta = new ServiceMeta(matchAll, ResourceActiveType.NORMAL_RESOURCE_TYPE);
                serviceMetaList.add(serviceMeta);
            }
            if (CollectionUtils.isNotEmpty(serviceMetaList)){
                serviceMetaObject = new ServiceMetaObject();
                Collections.sort(serviceMetaList);
                serviceMetaObject.setServiceMetaList(serviceMetaList);
                String meta = JSON.toJSONString(serviceMetaList);
                serviceMetaObject.setMeta(meta);
                String md5 = DigestUtils.md5Hex(meta.getBytes(StandardCharsets.UTF_8));
                serviceMetaObject.setMd5OfList(md5);
            }
        }
    }

    public ServiceMetaObject getServiceMetaObject() {
        return serviceMetaObject;
    }
}
