package com.ly.utils;

import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.el.ELContext;
import javax.el.ExpressionFactory;
import javax.el.MethodExpression;
import javax.el.ValueExpression;

import javax.faces.application.Application;
import javax.faces.application.FacesMessage; 
import javax.faces.component.UIComponent;
import javax.faces.component.UIViewRoot;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;

import javax.faces.event.ActionEvent;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import oracle.adf.view.rich.component.rich.RichPopup;
import oracle.adf.view.rich.context.AdfFacesContext;

import org.apache.myfaces.trinidad.component.UIXCommand;
import org.apache.myfaces.trinidad.render.ExtendedRenderKitService;
import org.apache.myfaces.trinidad.util.Service;

/**
 * General useful static utilies for working with JSF.
 * NOTE: Updated to use JSF 1.2 ExpressionFactory.
 * @author hjy
 * $Id: JSFUtils.java 2383 2014-09-17 16:25:37
 */
public final class JSFUtils {

    private static final String NO_RESOURCE_FOUND = "Missing resource: ";
    /**
     *获取el表达式
     * @param el
     * @return
     */
    public static Object getElExpression(String el) {      
           FacesContext facesContext = FacesContext.getCurrentInstance();
           //facesContext.responseComplete();
           ELContext elContext = facesContext.getELContext();
           ExpressionFactory expressionFactory =  facesContext.getApplication().getExpressionFactory();        
           ValueExpression valueExp = expressionFactory.createValueExpression(elContext,el,Object.class);
           return valueExp.getValue(elContext);
       }
    /**
     * 根据一个jsf绑定表达式返回其匹配对象
     * Method for taking a reference to a JSF binding expression and returning
     * the matching object (or creating it).
     * @param expression EL expression
     * @return Managed object
     */
    public static Object resolveExpression(String expression) {
        FacesContext facesContext = getFacesContext();
        Application app = facesContext.getApplication();
        ExpressionFactory elFactory = app.getExpressionFactory();
        ELContext elContext = facesContext.getELContext();
        ValueExpression valueExp =
            elFactory.createValueExpression(elContext, expression,
                                            Object.class);
        if(valueExp!=null){
            return valueExp.getValue(elContext);
        }else{
            return null;
        }
    }

    public static String resolveRemoteUser() {
        FacesContext facesContext = getFacesContext();
        ExternalContext ectx = facesContext.getExternalContext();
        return ectx.getRemoteUser();
    }

    public static String resolveUserPrincipal() {
        FacesContext facesContext = getFacesContext();
        ExternalContext ectx = facesContext.getExternalContext();
        HttpServletRequest request = (HttpServletRequest)ectx.getRequest();
        return request.getUserPrincipal().getName();
    }

	  /**
     * 返回Reponse对象
     */
    public static HttpServletResponse getResponse(){
        FacesContext ctx = FacesContext.getCurrentInstance();  
        HttpServletResponse response =  
                   (HttpServletResponse)ctx.getExternalContext().getResponse();
        return  response;
    }
    /**
     * 返回Request对象
     */
    public static HttpServletRequest getRequest(){
        FacesContext ctx = FacesContext.getCurrentInstance();  
        HttpServletRequest request =  
                   (HttpServletRequest)ctx.getExternalContext().getRequest();
        return  request;
    }
    
    public static Object resloveMethodExpression(String expression,
                                                 Class returnType,
                                                 Class[] argTypes,
                                                 Object[] argValues) {
        FacesContext facesContext = getFacesContext();
        Application app = facesContext.getApplication();
        ExpressionFactory elFactory = app.getExpressionFactory();
        ELContext elContext = facesContext.getELContext();
        MethodExpression methodExpression =
            elFactory.createMethodExpression(elContext, expression, returnType,
                                             argTypes);
        return methodExpression.invoke(elContext, argValues);
    }

    /**
     * 根据一个jsf绑定表达式返回其匹配布尔值
     * Method for taking a reference to a JSF binding expression and returning
     * the matching Boolean.
     * @param expression EL expression
     * @return Managed object
     */
    public static Boolean resolveExpressionAsBoolean(String expression) {
        return (Boolean)resolveExpression(expression);
    }

    /**根据一个jsf绑定表达式返回其匹配字符串
     * Method for taking a reference to a JSF binding expression and returning
     * the matching String (or creating it).
     * @param expression EL expression
     * @return Managed object
     */
    public static String resolveExpressionAsString(String expression) {
        return (String)resolveExpression(expression);
    }

    /**
     * Convenience method for resolving a reference to a managed bean by name
     * rather than by expression.
     * @param beanName name of managed bean
     * @return Managed object
     */
    public static Object getManagedBeanValue(String beanName) {
        StringBuffer buff = new StringBuffer("#{");
        buff.append(beanName);
        buff.append("}");
        return resolveExpression(buff.toString());
    }

    /**设置一个新的对象到jsf managedBean中
     * Method for setting a new object into a JSF managed bean
     * Note: will fail silently if the supplied object does
     * not match the type of the managed bean.
     * @param expression EL expression
     * @param newValue new value to set
     */
    public static void setExpressionValue(String expression, Object newValue) {
        FacesContext facesContext = getFacesContext();
        Application app = facesContext.getApplication();
        ExpressionFactory elFactory = app.getExpressionFactory();
        ELContext elContext = facesContext.getELContext();
        ValueExpression valueExp =
            elFactory.createValueExpression(elContext, expression,
                                            Object.class);

        //Check that the input newValue can be cast to the property type
        //expected by the managed bean.
        //If the managed Bean expects a primitive we rely on Auto-Unboxing
        //I could do a more comprehensive check and conversion from the object
        //to the equivilent primitive but life is too short
        Class bindClass = valueExp.getType(elContext);
        if (bindClass.isPrimitive() || bindClass.isInstance(newValue)) {
            valueExp.setValue(elContext, newValue);
        }
    }

    /**
     * 
     * Convenience method for setting the value of a managed bean by name
     * rather than by expression.
     * @param beanName name of managed bean
     * @param newValue new value to set
     */
    public static void setManagedBeanValue(String beanName, Object newValue) {
        StringBuffer buff = new StringBuffer("#{");
        buff.append(beanName);
        buff.append("}");
        setExpressionValue(buff.toString(), newValue);
    }


    /**
     * 设置session变量
     * Convenience method for setting Session variables.
     * @param key object key
     * @param object value to store
     */
    public static void storeOnSession(String key, Object object) {
        FacesContext ctx = getFacesContext();
        //Map sessionState = ctx.getExternalContext().getSessionMap();
       HttpServletRequest request =  (HttpServletRequest)ctx.getExternalContext().getRequest();
      HttpSession session =  request.getSession();
        session.setAttribute(key, object);
    }

    /**
     * 获取session变量
     * Convenience method for getting Session variables.
     * @param key object key
     * @return session object for key
     */
    public static Object getFromSession(String key) {

        FacesContext ctx = getFacesContext();
        //Map sessionState = ctx.getExternalContext().getSessionMap();
        HttpServletRequest request =  (HttpServletRequest)ctx.getExternalContext().getRequest();
        HttpSession sessionState =  request.getSession();
        return sessionState.getAttribute(key);
    }

    public static String getFromHeader(String key) {
        FacesContext ctx = getFacesContext();
        ExternalContext ectx = ctx.getExternalContext();
        return ectx.getRequestHeaderMap().get(key);
    }
    public static Object getSession() {
         return FacesContext.getCurrentInstance().getExternalContext().getSession(true);
     }

    /**获取请求变量
     * Convenience method for getting Request variables.
     * @param key object key
     * @return session object for key
     */
    public static Object getFromRequest(String key) {
        FacesContext ctx = getFacesContext();
        Map sessionState = ctx.getExternalContext().getRequestMap();
        return sessionState.get(key);
    }

    /**
     * Pulls a String resource from the property bundle that
     * is defined under the application &lt;message-bundle&gt; element in
     * the faces config. Respects Locale
     * @param key string message key
     * @return Resource value or placeholder error String
     */
    public static String getStringFromBundle(String key) {
        ResourceBundle bundle = getBundle();
        return getStringSafely(bundle, key, null);
    }


    /**
     * Convenience method to construct a <code>FacesMesssage</code>
     * from a defined error key and severity
     * This assumes that the error keys follow the convention of
     * using <b>_detail</b> for the detailed part of the
     * message, otherwise the main message is returned for the
     * detail as well.
     * @param key for the error message in the resource bundle
     * @param severity severity of message
     * @return Faces Message object
     */
    public static FacesMessage getMessageFromBundle(String key,
                                                    FacesMessage.Severity severity) {
        ResourceBundle bundle = getBundle();
        String summary = getStringSafely(bundle, key, null);
        String detail = getStringSafely(bundle, key + "_detail", summary);
        FacesMessage message = new FacesMessage(summary, detail);
        message.setSeverity(severity);
        return message;
    }

    /**
     * 添加信息消息
     * Add JSF info message.
     * @param msg info message string
     */
    public static void addFacesInformationMessage(String msg) {
        FacesContext ctx = getFacesContext();
        FacesMessage fm =
            new FacesMessage(FacesMessage.SEVERITY_INFO, msg, "");
        ctx.addMessage(getRootViewComponentId(), fm);
    }

    /**
     * 添加错误消息
     * Add JSF error message.
     * @param msg error message string
     */
    public static void addFacesErrorMessage(String msg) {
        FacesContext ctx = getFacesContext();
        FacesMessage fm =
            new FacesMessage(FacesMessage.SEVERITY_ERROR, msg, "");
        ctx.addMessage(getRootViewComponentId(), fm);
    }

    /**
     * 为指定属性添加错误消息
     * Add JSF error message for a specific attribute.
     * @param attrName name of attribute
     * @param msg error message string
     */
    public static void addFacesErrorMessage(String attrName, String msg) {
        // : Need a way to associate attribute specific messages
        //       with the UIComponent's Id! For now, just using the view id.
        //: make this use the internal getMessageFromBundle?
        FacesContext ctx = getFacesContext();
        FacesMessage fm =
            new FacesMessage(FacesMessage.SEVERITY_ERROR, attrName, msg);
        ctx.addMessage(getRootViewComponentId(), fm);
    }
    
    /**
     * Add JSF error message for a specific attribute.
     * @param attrName name of attribute
     * @param msg error message string
     */
    public static void addFacesErrorMessage(String compId,String attrName, String msg) {
        // : Need a way to associate attribute specific messages
        //       with the UIComponent's Id! For now, just using the view id.
        //: make this use the internal getMessageFromBundle?
        FacesContext ctx = getFacesContext();
        FacesMessage fm =
            new FacesMessage(FacesMessage.SEVERITY_ERROR, attrName, msg);
        ctx.addMessage(compId, fm);
    }
    
    /**
     * 错误提示
     * @param msg 提示信息
     * @param id  提示信息显示在某个组件的上（组件ID）
     */
    public static void addErrorMsg(String msg,String id){
        FacesMessage fm =
            new FacesMessage(msg);
        fm.setSeverity(FacesMessage.SEVERITY_ERROR);
        FacesContext context = FacesContext.getCurrentInstance();
        
        context.addMessage(id, fm);
    }
    // Informational getters

    /**
     * 获取view id
     * Get view id of the view root.
     * @return view id of the view root
     */
    public static String getRootViewId() {
        return getFacesContext().getViewRoot().getViewId();
    }

    /**
     * 
     * Get component id of the view root.
     * @return component id of the view root
     */
    public static String getRootViewComponentId() {
        return getFacesContext().getViewRoot().getId();
    }

    /**
     * 获取faces上下文
     * Get FacesContext.
     * @return FacesContext
     */
    public static FacesContext getFacesContext() {
        return FacesContext.getCurrentInstance();
    }
    /*
   * Internal method to pull out the correct local
   * message bundle
   */

    private static ResourceBundle getBundle() {
        FacesContext ctx = getFacesContext();
        UIViewRoot uiRoot = ctx.getViewRoot();
        Locale locale = uiRoot.getLocale();
        ClassLoader ldr = Thread.currentThread().getContextClassLoader();
        return ResourceBundle.getBundle(ctx.getApplication().getMessageBundle(),
                                        locale, ldr);
    }

    /**
     * 获取一个http请求属性
     * Get an HTTP Request attribute.
     * @param name attribute name
     * @return attribute value
     */
    public static Object getRequestAttribute(String name) {
        return getFacesContext().getExternalContext().getRequestMap().get(name);
    }

    /**
     * 设置一个http请求属性
     * Set an HTTP Request attribute.
     * @param name attribute name
     * @param value attribute value
     */
    public static void setRequestAttribute(String name, Object value) {
        getFacesContext().getExternalContext().getRequestMap().put(name,
                                                                   value);
    }

    /*
   * Internal method to proxy for resource keys that don't exist
   */

    public static String getStringSafely(ResourceBundle bundle, String key,
                                         String defaultValue) {
        String resource = null;
        try {
            resource = bundle.getString(key);
        } catch (MissingResourceException mrex) {
            if (defaultValue != null) {
                resource = defaultValue;
            } else {
                resource = NO_RESOURCE_FOUND + key;
            }
        }
        return resource;
    }

    /**
     * 根据组件id查找一个组件
     * Locate an UIComponent in view root with its component id. Use a recursive way to achieve this.
     * Taken from http://www.jroller.com/page/mert?entry=how_to_find_a_uicomponent
     * @param id UIComponent id
     * @return UIComponent object
     */
    public static UIComponent findComponentInRoot(String id) {
        UIComponent component = null;
        FacesContext facesContext = FacesContext.getCurrentInstance();
        if (facesContext != null) {
            UIComponent root = facesContext.getViewRoot();
            component = findComponent(root, id);
        }
        return component;
    }

    /**
     * 从根组件中查找一个组件
     * Locate an UIComponent from its root component.
     * Taken from http://www.jroller.com/page/mert?entry=how_to_find_a_uicomponent
     * @param base root Component (parent)
     * @param id UIComponent id
     * @return UIComponent object
     */
    public static UIComponent findComponent(UIComponent base, String id) {
        if (id.equals(base.getId()))
            return base;

        UIComponent children = null;
        UIComponent result = null;
        Iterator childrens = base.getFacetsAndChildren();
        while (childrens.hasNext() && (result == null)) {
            children = (UIComponent)childrens.next();
            if (id.equals(children.getId())) {
                result = children;
                break;
            }
            result = findComponent(children, id);
            if (result != null) {
                break;
            }
        }
        return result;
    }

    /**
     * 创建一个重定向url
     * Method to create a redirect URL. The assumption is that the JSF servlet mapping is
     * "faces", which is the default
     *
     * @param view the JSP or JSPX page to redirect to
     * @return a URL to redirect to
     */
    public static String getPageURL(String view) {
        FacesContext facesContext = getFacesContext();
        ExternalContext externalContext = facesContext.getExternalContext();
        String url =
            ((HttpServletRequest)externalContext.getRequest()).getRequestURL().toString();
        StringBuffer newUrlBuffer = new StringBuffer();
        newUrlBuffer.append(url.substring(0, url.lastIndexOf("faces/")));
        newUrlBuffer.append("faces");
        String targetPageUrl = view.startsWith("/") ? view : "/" + view;
        newUrlBuffer.append(targetPageUrl);
        return newUrlBuffer.toString();
    }
    /**
     * 获取当前工程的根目录
     * @return
     * 
     */
    public static String getRealPath(){
        FacesContext fctx = FacesContext.getCurrentInstance();
        ServletContext servletCtx =
            (ServletContext)fctx.getExternalContext().getContext();
        String dirPath = servletCtx.getRealPath("/");
        return dirPath;
    }
    
    public static Object invokeMethodExpression(String expr, Class returnType, Class[] argTypes,Object[] args){
        FacesContext fc = FacesContext.getCurrentInstance(); 
        ELContext elctx = fc.getELContext();
        ExpressionFactory elFactory = fc.getApplication().getExpressionFactory(); 
        MethodExpression methodExpr = elFactory.createMethodExpression(elctx,expr,returnType,argTypes); 
    return methodExpr.invoke(elctx,args); 
    }
    
    public static Object invokeMethodExpression(String expr, Class returnType,Class argType, Object argument){     
           return invokeMethodExpression(expr, returnType,new Class[]{argType}, new Object[]{argument});
       }
    
    /**
     *刷新组件
     * @param components
     */
    public static void refresh(UIComponent... components) {
        for (UIComponent temp : components) {
            AdfFacesContext.getCurrentInstance().addPartialTarget(temp);
        }
    }


    /**
     * @function ManagedBean中引用此方法，传入的PopUp对象作参数，页面上将弹出与此参数对象绑定的PopUp组件。
     * @param pop
     * @return null
     */
    public static void showPopup(RichPopup pop) {
        FacesContext context = FacesContext.getCurrentInstance();
        String popupId = pop.getClientId(context);
        StringBuilder script = new StringBuilder();
        script.append("var popup = AdfPage.PAGE.findComponent('").append(popupId).append("'); ").append("if (!popup.isPopupVisible()) { ").append("var hints = {}; ").append("popup.show(hints);}");
        ExtendedRenderKitService erks =
            Service.getService(context.getRenderKit(),
                               ExtendedRenderKitService.class);
        erks.addScript(context, script.toString());
    }

    /**
     *关闭pop
     * @param pop
     */
    public static void closePopup(RichPopup pop) {
        FacesContext context = FacesContext.getCurrentInstance();
        String popupId = pop.getClientId(context);
        StringBuilder script = new StringBuilder();
        script.append("var popup = AdfPage.PAGE.findComponent('").append(popupId).append("'); ").append("if (popup.isPopupVisible()) { ").append("var hints = {}; ").append("popup.hide();}");
        ExtendedRenderKitService erks =
            Service.getService(context.getRenderKit(),
                               ExtendedRenderKitService.class);
        erks.addScript(context, script.toString());
    }

    /**
     * Add a script to the render kit. Usually you would put this in a helper class
     */

    public static void addScriptOnPartialRequest(String script) {
        FacesContext context = FacesContext.getCurrentInstance();
        if (AdfFacesContext.getCurrentInstance().isPartialRequest(context)) {
            ExtendedRenderKitService erks =
                Service.getRenderKitService(context,
                                            ExtendedRenderKitService.class);
            erks.addScript(context, script);
        }
    }

    /**
     * Button程序触发点击事件,可以通过程序来执行click事件
     * @param btn RichButton
     */
    public static void doAction(UIXCommand btn) {
        if (btn == null) {
            return;
        } else {
            ActionEvent ae = new ActionEvent(btn);
            ae.queue();
        }
    }
    
    /**
     * 得到web.xml里的context-param参数
     * @param str
     * @return
     */
    public static String getInitParameter(String str) {
        FacesContext ctx = FacesContext.getCurrentInstance();
        String myConstantValue = ctx.getExternalContext().getInitParameter(str);
        if (myConstantValue != null) {
            return myConstantValue;
        }
        return "";
    }
    /**
     *获取登录地址
     * @param request
     * @return
     */
    public static String getIpAddr() {
       HttpServletRequest request= JSFUtils.getRequest();
        try {
            String ip = request.getHeader("X-Forwarded-For");
            if (ip == null || ip.length() == 0 ||
                "unknown".equalsIgnoreCase(ip)) {
                ip = request.getHeader("Proxy-Client-IP");
            }
            if (ip == null || ip.length() == 0 ||
                "unknown".equalsIgnoreCase(ip)) {
                ip = request.getHeader("WL-Proxy-Client-IP");
            }
            if (ip == null || ip.length() == 0 ||
                "unknown".equalsIgnoreCase(ip)) {
                ip = request.getHeader("HTTP_CLIENT_IP");
            }
            if (ip == null || ip.length() == 0 ||
                "unknown".equalsIgnoreCase(ip)) {
                ip = request.getHeader("HTTP_X_FORWARDED_FOR");
            }
            if (ip == null || ip.length() == 0 ||
                "unknown".equalsIgnoreCase(ip)) {
                ip = request.getRemoteAddr();
            }
            return ip;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}




