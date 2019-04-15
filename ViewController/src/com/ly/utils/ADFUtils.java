package com.ly.utils;


import java.io.Serializable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.el.ELContext;
import javax.el.ExpressionFactory;
import javax.el.MethodExpression;
import javax.el.ValueExpression;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;

import oracle.adf.model.BindingContext;
import oracle.adf.model.binding.DCBindingContainer;
import oracle.adf.model.binding.DCIteratorBinding;
import oracle.adf.model.binding.DCParameter;
import oracle.adf.share.logging.ADFLogger;
import oracle.adf.view.rich.component.rich.RichPopup;
import oracle.adf.view.rich.component.rich.data.RichTable;
import oracle.adf.view.rich.context.AdfFacesContext;

import oracle.binding.AttributeBinding;
import oracle.binding.BindingContainer;
import oracle.binding.ControlBinding;
import oracle.binding.OperationBinding;

import oracle.jbo.ApplicationModule;
import oracle.jbo.Key;
import oracle.jbo.NavigatableRowIterator;
import oracle.jbo.Row;
import oracle.jbo.RowSetIterator;
import oracle.jbo.ViewObject;
import oracle.jbo.uicli.binding.JUCtrlHierBinding;
import oracle.jbo.uicli.binding.JUCtrlHierNodeBinding;
import oracle.jbo.uicli.binding.JUCtrlListBinding;
import oracle.jbo.uicli.binding.JUCtrlValueBinding;
import oracle.jbo.uicli.binding.JUIteratorBinding;

import org.apache.myfaces.trinidad.model.CollectionModel;
import org.apache.myfaces.trinidad.model.RowKeySet;
import org.apache.myfaces.trinidad.model.RowKeySetImpl;
import org.apache.myfaces.trinidad.render.ExtendedRenderKitService;
import org.apache.myfaces.trinidad.util.Service;

public class ADFUtils {


    public static final ADFLogger LOGGER =
        ADFLogger.createADFLogger(ADFUtils.class);

    /**
     * 根据Data control的名字获取对应的AM（常用）
     * Get application module for an application module data control by name.
     * @param name application module data control name
     * @return ApplicationModule
     */
    public static ApplicationModule getApplicationModuleForDataControl(String name) {
        Object obj =
            JSFUtils.resolveExpression("#{data." + name + ".dataProvider}");
        if (obj != null) {
            return (ApplicationModule)obj;
        } else {
            return null; //new
        }
    }

    /**
     * 获取当前页面范围内绑定属性的值（常用）
     * A convenience method for getting the value of a bound attribute in the
     * current page context programatically.
     * @param attributeName of the bound value in the pageDef
     * @return value of the attribute
     */

    public static Object getBoundAttributeValue(String attributeName) {
        return findControlBinding(attributeName).getInputValue();
    }

    /**
     * 在当前页面范围内设置一个绑定属性的值（常用）
     * A convenience method for setting the value of a bound attribute in the
     * context of the current page.
     * @param attributeName of the bound value in the pageDef
     * @param value to set
     */
    public static void setBoundAttributeValue(String attributeName,
                                              Object value) {
        findControlBinding(attributeName).setInputValue(value);
    }

    /**
     * 返回页面定义文件参数的评定值
     * Returns the evaluated value of a pageDef parameter.
     * @param pageDefName reference to the page definition file of the page with the parameter
     * @param parameterName name of the pagedef parameter
     * @return evaluated value of the parameter as a String
     */
    public static Object getPageDefParameterValue(String pageDefName,
                                                  String parameterName) {
        BindingContainer bindings = findBindingContainer(pageDefName);
        DCParameter param =
            ((DCBindingContainer)bindings).findParameter(parameterName);
        return param.getValue();
    }

    /**
     * 查找一个DCControl绑定来作为属性绑定以方便调用其getInputValue和setInputValue
     * Convenience method to find a DCControlBinding as an AttributeBinding
     * to get able to then call getInputValue() or setInputValue() on it.
     * @param bindingContainer binding container binding容器
     * @param attributeName name of the attribute binding. 绑定属性的属性名称
     * @return the control value binding with the name passed in.
     *
     */
    public static AttributeBinding findControlBinding(BindingContainer bindingContainer,
                                                      String attributeName) {
        if (attributeName != null) {
            if (bindingContainer != null) {
                ControlBinding ctrlBinding =
                    bindingContainer.getControlBinding(attributeName);
                if (ctrlBinding instanceof AttributeBinding) {
                    return (AttributeBinding)ctrlBinding;
                }
            }
        }
        return null;
    }

    /**
     * 查找一个DCControl绑定作为JUCtrlValueBinding以方便调用其getInputValue和setInputValue
     * Convenience method to find a DCControlBinding as a JUCtrlValueBinding
     * to get able to then call getInputValue() or setInputValue() on it.
     * @param attributeName name of the attribute binding.
     * @return the control value binding with the name passed in.
     *
     */
    public static AttributeBinding findControlBinding(String attributeName) {
        return findControlBinding(getBindingContainer(), attributeName);
    }

    /**
     * 返回当前页面的绑定容器（常用）
     * Return the current page's binding container.
     * @return the current page's binding container
     */
    public static BindingContainer getBindingContainer() {
        return (BindingContainer)JSFUtils.resolveExpression("#{bindings}");
    }

    /**
     * 返回DC绑定容器
     * Return the Binding Container as a DCBindingContainer.
     * @return current binding container as a DCBindingContainer
     */
    public static DCBindingContainer getDCBindingContainer() {
        return (DCBindingContainer)getBindingContainer();
    }

    /**
     * 获取table记录数（常用）
     * @param iteratorBinding table绑定的迭代器
     * @return
     */
    public static int getTableCount(String iteratorBinding) {
        DCBindingContainer bindings =
            (DCBindingContainer)BindingContext.getCurrent().getCurrentBindingsEntry();
        DCIteratorBinding yourVOIterator =
            bindings.findIteratorBinding(iteratorBinding);
        RowSetIterator yourVORowSetIterator =
            yourVOIterator.getRowSetIterator();
        return yourVORowSetIterator.getEstimatedRangePageCount();

    }

    /**
     * 获取迭代绑定的选择项列表
     * Get List of ADF Faces SelectItem for an iterator binding.
     *
     * Uses the value of the 'valueAttrName' attribute as the key for
     * the SelectItem key.
     *
     * @param iteratorName ADF iterator binding name
     * @param valueAttrName name of the value attribute to use
     * @param displayAttrName name of the attribute from iterator rows to display
     * @return ADF Faces SelectItem for an iterator binding
     */
    public static List<SelectItem> selectItemsForIterator(String iteratorName,
                                                          String valueAttrName,
                                                          String displayAttrName) {
        return selectItemsForIterator(findIterator(iteratorName),
                                      valueAttrName, displayAttrName);
    }

    /**
     *
     * Get List of ADF Faces SelectItem for an iterator binding with description.
     *
     * Uses the value of the 'valueAttrName' attribute as the key for
     * the SelectItem key.
     *
     * @param iteratorName ADF iterator binding name
     * @param valueAttrName name of the value attribute to use
     * @param displayAttrName name of the attribute from iterator rows to display
     * @param descriptionAttrName name of the attribute to use for description
     * @return ADF Faces SelectItem for an iterator binding with description
     */
    public static List<SelectItem> selectItemsForIterator(String iteratorName,
                                                          String valueAttrName,
                                                          String displayAttrName,
                                                          String descriptionAttrName) {
        return selectItemsForIterator(findIterator(iteratorName),
                                      valueAttrName, displayAttrName,
                                      descriptionAttrName);
    }

    /**
     * 获取一个迭代的属性值列表
     * Get List of attribute values for an iterator.
     * @param iteratorName ADF iterator binding name
     * @param valueAttrName value attribute to use
     * @return List of attribute values for an iterator
     */
    public static List attributeListForIterator(String iteratorName,
                                                String valueAttrName) {
        return attributeListForIterator(findIterator(iteratorName),
                                        valueAttrName);
    }

    /**
     * 获取一个迭代行的关键对象列表
     * Get List of Key objects for rows in an iterator.
     * @param iteratorName iterabot binding name
     * @return List of Key objects for rows
     */
    public static List<Key> keyListForIterator(String iteratorName) {
        return keyListForIterator(findIterator(iteratorName));
    }

    /**
     * 获取一个迭代行的关键对象列表
     * Get List of Key objects for rows in an iterator.
     * @param iter iterator binding
     * @return List of Key objects for rows
     */
    public static List<Key> keyListForIterator(DCIteratorBinding iter) {
        List<Key> attributeList = new ArrayList<Key>();
        for (Row r : iter.getAllRowsInRange()) {
            attributeList.add(r.getKey());
        }
        return attributeList;
    }

    /**
     * 使用关键属性获取一个迭代行的关键对象列表
     * Get List of Key objects for rows in an iterator using key attribute.
     * @param iteratorName iterator binding name
     * @param keyAttrName name of key attribute to use
     * @return List of Key objects for rows
     */
    public static List<Key> keyAttrListForIterator(String iteratorName,
                                                   String keyAttrName) {
        return keyAttrListForIterator(findIterator(iteratorName), keyAttrName);
    }

    /**使用关键属性获取一个迭代行的关键对象列表
     * Get List of Key objects for rows in an iterator using key attribute.
     *
     * @param iter iterator binding
     * @param keyAttrName name of key attribute to use
     * @return List of Key objects for rows
     */
    public static List<Key> keyAttrListForIterator(DCIteratorBinding iter,
                                                   String keyAttrName) {
        List<Key> attributeList = new ArrayList<Key>();
        for (Row r : iter.getAllRowsInRange()) {
            attributeList.add(new Key(new Object[] { r.getAttribute(keyAttrName) }));
        }
        return attributeList;
    }

    /**
     * 获取一个迭代的属性值列表
     * Get a List of attribute values for an iterator.
     *
     * @param iter iterator binding
     * @param valueAttrName name of value attribute to use
     * @return List of attribute values
     */
    public static List attributeListForIterator(DCIteratorBinding iter,
                                                String valueAttrName) {
        List attributeList = new ArrayList();
        for (Row r : iter.getAllRowsInRange()) {
            attributeList.add(r.getAttribute(valueAttrName));
        }
        return attributeList;
    }

    /**
     * 根据名称在当前绑定容器内找到一个迭代器绑定
     * Find an iterator binding in the current binding container by name.
     *
     * @param name iterator binding name
     * @return iterator binding
     */
    public static DCIteratorBinding findIterator(String name) {
        DCIteratorBinding iter =
            getDCBindingContainer().findIteratorBinding(name);
        if (iter == null) {
            throw new RuntimeException("Iterator '" + name + "' not found");
        }

        return iter;
    }

    /**
     * @param bindingContainer
     * @param iterator
     * @return
     */
    public static DCIteratorBinding findIterator(String bindingContainer,
                                                 String iterator) {
        DCBindingContainer bindings =
            (DCBindingContainer)JSFUtils.resolveExpression("#{" +
                                                           bindingContainer +
                                                           "}");
        if (bindings == null) {
            throw new RuntimeException("Binding container '" +
                                       bindingContainer + "' not found");
        }
        DCIteratorBinding iter = bindings.findIteratorBinding(iterator);
        if (iter == null) {
            throw new RuntimeException("Iterator '" + iterator +
                                       "' not found");
        }
        return iter;
    }

    /**
     * @param name
     * @return
     */
    public static JUCtrlValueBinding findCtrlBinding(String name) {
        JUCtrlValueBinding rowBinding =
            (JUCtrlValueBinding)getDCBindingContainer().findCtrlBinding(name);
        if (rowBinding == null) {
            throw new RuntimeException("CtrlBinding " + name + "' not found");
        }
        return rowBinding;
    }

    /**
     * 在当前绑定容器内根据名称查找一个操作绑定（常用）
     * Find an operation binding in the current binding container by name.
     *
     * @param name operation binding name
     * @return operation binding
     */
    public static OperationBinding findOperation(String name) {
        OperationBinding op =
            getDCBindingContainer().getOperationBinding(name);
        if (op == null) {
            throw new RuntimeException("Operation '" + name + "' not found");
        }
        return op;
    }

    /**
     *根据操作名称执行操作
     * @param operationName 操作名称
     */
    public static void executeByOperation(String operationName) {
        OperationBinding op = findOperation(operationName);
        if (op == null) {
            throw new RuntimeException("Operation '" + operationName +
                                       "' not found");
        } else {
            op.execute();
        }
    }

    /**
     * 在当前绑定容器内根据名称查找一个操作绑定（常用）
     * Find an operation binding in the current binding container by name.
     *
     * @param bindingContianer binding container name
     * @param opName operation binding name
     * @return operation binding
     */
    public static OperationBinding findOperation(String bindingContianer,
                                                 String opName) {
        DCBindingContainer bindings =
            (DCBindingContainer)JSFUtils.resolveExpression("#{" +
                                                           bindingContianer +
                                                           "}");
        if (bindings == null) {
            throw new RuntimeException("Binding container '" +
                                       bindingContianer + "' not found");
        }
        OperationBinding op = bindings.getOperationBinding(opName);
        if (op == null) {
            throw new RuntimeException("Operation '" + opName + "' not found");
        }
        return op;
    }

    /**
     * Get List of ADF Faces SelectItem for an iterator binding with description.
     *
     * Uses the value of the 'valueAttrName' attribute as the key for
     * the SelectItem key.
     *
     * @param iter ADF iterator binding
     * @param valueAttrName name of value attribute to use for key
     * @param displayAttrName name of the attribute from iterator rows to display
     * @param descriptionAttrName name of the attribute for description
     * @return ADF Faces SelectItem for an iterator binding with description
     */
    public static List<SelectItem> selectItemsForIterator(DCIteratorBinding iter,
                                                          String valueAttrName,
                                                          String displayAttrName,
                                                          String descriptionAttrName) {
        List<SelectItem> selectItems = new ArrayList<SelectItem>();
        for (Row r : iter.getAllRowsInRange()) {
            selectItems.add(new SelectItem(r.getAttribute(valueAttrName),
                                           (String)r.getAttribute(displayAttrName),
                                           (String)r.getAttribute(descriptionAttrName)));
        }
        return selectItems;
    }

    /**
     * 获取一个迭代的选择项列表
     * Get List of ADF Faces SelectItem for an iterator binding.
     *
     * Uses the value of the 'valueAttrName' attribute as the key for
     * the SelectItem key.
     *
     * @param iter ADF iterator binding
     * @param valueAttrName name of value attribute to use for key
     * @param displayAttrName name of the attribute from iterator rows to display
     * @return ADF Faces SelectItem for an iterator binding
     */
    public static List<SelectItem> selectItemsForIterator(DCIteratorBinding iter,
                                                          String valueAttrName,
                                                          String displayAttrName) {
        List<SelectItem> selectItems = new ArrayList<SelectItem>();
        for (Row r : iter.getAllRowsInRange()) {
            selectItems.add(new SelectItem(r.getAttribute(valueAttrName),
                                           (String)r.getAttribute(displayAttrName)));
        }
        return selectItems;
    }

    /**
     * 获取一个迭代绑定的选择项列表
     * Get List of ADF Faces SelectItem for an iterator binding.
     *
     * Uses the rowKey of each row as the SelectItem key.
     *
     * @param iteratorName ADF iterator binding name
     * @param displayAttrName name of the attribute from iterator rows to display
     * @return ADF Faces SelectItem for an iterator binding
     */
    public static List<SelectItem> selectItemsByKeyForIterator(String iteratorName,
                                                               String displayAttrName) {
        return selectItemsByKeyForIterator(findIterator(iteratorName),
                                           displayAttrName);
    }

    /**
     * Get List of ADF Faces SelectItem for an iterator binding with discription.
     *
     * Uses the rowKey of each row as the SelectItem key.
     *
     * @param iteratorName ADF iterator binding name
     * @param displayAttrName name of the attribute from iterator rows to display
     * @param descriptionAttrName name of the attribute for description
     * @return ADF Faces SelectItem for an iterator binding with discription
     */
    public static List<SelectItem> selectItemsByKeyForIterator(String iteratorName,
                                                               String displayAttrName,
                                                               String descriptionAttrName) {
        return selectItemsByKeyForIterator(findIterator(iteratorName),
                                           displayAttrName,
                                           descriptionAttrName);
    }

    /**
     * Get List of ADF Faces SelectItem for an iterator binding with discription.
     *
     * Uses the rowKey of each row as the SelectItem key.
     *
     * @param iter ADF iterator binding
     * @param displayAttrName name of the attribute from iterator rows to display
     * @param descriptionAttrName name of the attribute for description
     * @return ADF Faces SelectItem for an iterator binding with discription
     */
    public static List<SelectItem> selectItemsByKeyForIterator(DCIteratorBinding iter,
                                                               String displayAttrName,
                                                               String descriptionAttrName) {
        List<SelectItem> selectItems = new ArrayList<SelectItem>();
        for (Row r : iter.getAllRowsInRange()) {
            selectItems.add(new SelectItem(r.getKey(),
                                           (String)r.getAttribute(displayAttrName),
                                           (String)r.getAttribute(descriptionAttrName)));
        }
        return selectItems;
    }

    /**
     * 获取一个迭代的选择项列表
     * Get List of ADF Faces SelectItem for an iterator binding.
     *
     * Uses the rowKey of each row as the SelectItem key.
     *
     * @param iter ADF iterator binding
     * @param displayAttrName name of the attribute from iterator rows to display
     * @return List of ADF Faces SelectItem for an iterator binding
     */
    public static List<SelectItem> selectItemsByKeyForIterator(DCIteratorBinding iter,
                                                               String displayAttrName) {
        List<SelectItem> selectItems = new ArrayList<SelectItem>();
        for (Row r : iter.getAllRowsInRange()) {
            selectItems.add(new SelectItem(r.getKey(),
                                           (String)r.getAttribute(displayAttrName)));
        }
        return selectItems;
    }

    /**
     * 根据名称查找一个页面定义的绑定容器
     * Find the BindingContainer for a page definition by name.
     *
     * Typically used to refer eagerly to page definition parameters. It is
     * not best practice to reference or set bindings in binding containers
     * that are not the one for the current page.
     *
     * @param pageDefName name of the page defintion XML file to use
     * @return BindingContainer ref for the named definition
     */
    private static BindingContainer findBindingContainer(String pageDefName) {
        BindingContext bctx = getDCBindingContainer().getBindingContext();

        BindingContainer foundContainer =
            bctx.findBindingContainer(pageDefName);
        return foundContainer;
    }

    /**
     * @param opList
     */
    public static void printOperationBindingExceptions(List opList) {
        if (opList != null && !opList.isEmpty()) {
            for (Object error : opList) {
                LOGGER.severe(error.toString());
            }
        }
    }


    public static Object executeOperationBindingNHandleErr(String methodAction,
                                                           Map param) {
        OperationBinding ob = ADFUtils.findOperation(methodAction);

        if (param != null) {
            Map paramOps = ob.getParamsMap();
            paramOps.putAll(param);
        }
        Object result = ob.execute();
        if (!ob.getErrors().isEmpty()) {
            JSFUtils.addFacesErrorMessage("An error occured while serving your request.");
            ADFUtils.printOperationBindingExceptions(ob.getErrors());
        }
        return result;
    }

    public static boolean isBCTransactionDirty() {
        // get application module and check for dirty
        // transaction
        ApplicationModule am = ADFUtils.getDCBindingContainer().getDataControl().getApplicationModule();
        return am.getTransaction().isDirty();
    }

    public static boolean isControllerTransactionDirty() {
        // get data control and check for dirty transaction
        BindingContext bc = BindingContext.getCurrent();
        String currentDataControlFrame = bc.getCurrentDataControlFrame();
        return bc.findDataControlFrame(currentDataControlFrame).isTransactionDirty();
    }

    public static boolean hasChanges() {
        return isBCTransactionDirty() || isControllerTransactionDirty();
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

    public static NavigatableRowIterator getTableIter(RichTable table) {
        CollectionModel _tableModel = (CollectionModel)table.getValue();
        JUCtrlHierBinding _adfTableBinding =
            (JUCtrlHierBinding)_tableModel.getWrappedData();
        DCIteratorBinding it = _adfTableBinding.getDCIteratorBinding();
        return it.getNavigatableRowIterator();
    }

    /**
     *获取当前行
     * @param table
     * @return
     */
    public static Row getRow(RichTable table) {
        NavigatableRowIterator iterator = getTableIter(table);
        return iterator.getCurrentRow();
    }

    /**
     *获取选择列表值(多选)
     * @param attrName
     * @return
     */
    public static Object[] getSelectSingValue(String attrName) {
        BindingContext bctx = BindingContext.getCurrent();
        BindingContainer bindings = bctx.getCurrentBindingsEntry();
        JUCtrlListBinding selectValueList =
            (JUCtrlListBinding)bindings.get(attrName);

        Object[] selVals = selectValueList.getSelectedValues();
        return selVals;
    }
    /**
     * 返回页面下拉Lov对应的VO,用于对此Lov进行过滤
     * @param attrName 
     * @return
     */
    public static ViewObject filteLov(String attrName) {
        BindingContext bctx = BindingContext.getCurrent();
        BindingContainer bindings = bctx.getCurrentBindingsEntry();

        JUCtrlListBinding lov =
            (JUCtrlListBinding)bindings.get(attrName);
        return lov.getListIterBinding().getViewObject();
    }

    /**
     *获取选择列表值(单选)
     * @param attrName
     * @return
     * @deprecated
     */
    public static Object getSelectListValue(String attrName) {
        BindingContext bctx = BindingContext.getCurrent();
        BindingContainer bindings = bctx.getCurrentBindingsEntry();
        JUCtrlListBinding selectValueList =
            (JUCtrlListBinding)bindings.get(attrName);
      //  FacesCtrlListBinding lov = (FacesCtrlListBinding) bindings.get("JxglBzfpView1");

        Object selVals = selectValueList.getSelectedValue();
        return selVals;
    }
    /**
     * 用于拉vo在页面以下拉选择方式的选择值,单选
     * 
     * @param attrName:绑定vo的id
     * @param colName:选择行返回的列名
     * @return
     */
    public static Object getSelectListValueOne(String attrName,String colName){
        List list=getSelectListValueMany(attrName,colName);
        return list==null?null:list.get(0);
    }
    /**
     * 用于拉vo在页面以下拉选择方式的选择值,多选
     * @param attrName
     * @param colName
     * @return
     */
    public static List getSelectListValueMany(String attrName,String colName){
        BindingContext bctx = BindingContext.getCurrent();
        BindingContainer bindings = bctx.getCurrentBindingsEntry();
        JUCtrlListBinding selectValueList = (JUCtrlListBinding) bindings.get(attrName);
        
        int[] selVals = selectValueList.getSelectedIndices();
        List  list =new ArrayList();
        
        for(int i:selVals){
            list.add(selectValueList.getRowAtRangeIndex(i).getAttribute(colName));
        }
        return list;
    
    }
    /**
     * 用于查询的级联下拉
     * @param attrName
     * @param ind 选择的下拉索引值
     * @param retrunStr 返回的属性字段名称
     * @return
     */
    public static Object getSelectListValue2(String attrName, Object ind,
                                             String retrunStr) {
        BindingContext bctx = BindingContext.getCurrent();
        BindingContainer bindings = bctx.getCurrentBindingsEntry();
        JUCtrlListBinding listBinding =
            (JUCtrlListBinding)bindings.get(attrName);
        listBinding.setSelectedIndex(Integer.parseInt(ind.toString()));
        Row selectedValue = (Row)listBinding.getSelectedValue();
        return selectedValue.getAttribute(retrunStr);
    }

    /**
     *根据vo对象及主键id返回一条记录
     * @param vo vo对象
     * @param id  主键id
     * @return row  返回一条记录
     */
    public static Row getRowByKey(ViewObject vo, int id) {
        Row[] rows =
            vo.findByKey(new Key(new Object[] { id }), 1); //根据主键id获取一条记录
        Row row = rows[0];
        return row;
    }

    /**
     *获取绑定容器
     * @return
     */
    public BindingContainer getBindings() {
        return BindingContext.getCurrent().getCurrentBindingsEntry();
    }

    /**
     *刷新list所在table
     * @param iterator table所在vo对应的迭代器
     * @param listTable 列表table所绑定的组件变量
     */
    public static void refreshTable(String iterator, RichTable listTable) {
        DCBindingContainer xxDcBindings =
            (DCBindingContainer)BindingContext.getCurrent().getCurrentBindingsEntry();
        DCIteratorBinding xxIterBind =
            (DCIteratorBinding)xxDcBindings.get(iterator);
        xxIterBind.executeQuery();
        refresh(listTable);
    }
    
    /**
        *重定位行
        * @param richTable
        * @param oldKey
        */
       public static void remakeCurrentRow(RichTable richTable, Key oldKey) {
           CollectionModel cm = (CollectionModel)richTable.getValue();
           JUCtrlHierBinding juiterbinding =
               (JUCtrlHierBinding)cm.getWrappedData();
           RowKeySet rs = new RowKeySetImpl();
           ArrayList al = new ArrayList();
           al.add(oldKey);
           rs.add(al);
           richTable.setSelectedRowKeys(rs);
           JUIteratorBinding iter = juiterbinding.getIteratorBinding();
           iter.setCurrentRowWithKey(oldKey.toStringFormat(true));
       }
    
    
    
    /**
     * Programmatic invocation of a method that an EL evaluates to.
     *
     * @param el EL of the method to invoke
     * @param paramTypes Array of Class defining the types of the parameters
     * @param params Array of Object defining the values of the parametrs
     * @return Object that the method returns
     */
    public static Object invokeEL(String el, Class[] paramTypes, Object[] params) {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        ELContext elContext = facesContext.getELContext();
        ExpressionFactory expressionFactory = facesContext.getApplication().getExpressionFactory();
        MethodExpression exp = expressionFactory.createMethodExpression(elContext, el, Object.class, paramTypes);

        return exp.invoke(elContext, params);
    }
    
    /**
     * Programmatic evaluation of EL.
     *
     * @param el EL to evaluate
     * @return Result of the evaluation
     */
    public static Object evaluateEL(String el) {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        ELContext elContext = facesContext.getELContext();
        ExpressionFactory expressionFactory = facesContext.getApplication().getExpressionFactory();
        ValueExpression exp = expressionFactory.createValueExpression(elContext, el, Object.class);

        return exp.getValue(elContext);
    }
    
    /**
     * 创建Row(通过RichTable,比通过Binding层更好)
     * @return
     */
    public static Row createRowByRichTable(RichTable richTable) {
        CollectionModel cm = (CollectionModel)richTable.getValue();
        JUCtrlHierBinding jubind = (JUCtrlHierBinding)cm.getWrappedData();
        DCIteratorBinding dciteratorbind = jubind.getDCIteratorBinding();
        RowSetIterator rsiterator = dciteratorbind.getRowSetIterator();
        Row newRow = rsiterator.createRow();
        newRow.setNewRowState(Row.STATUS_INITIALIZED);
        rsiterator.insertRow(newRow);
        dciteratorbind.setCurrentRowWithKey(newRow.getKey().toStringFormat(true));

        RowKeySet newSet = new RowKeySetImpl();
        ArrayList newSelectedRowKey = new ArrayList();
        newSelectedRowKey.add(newRow.getKey());
        newSet.add(newSelectedRowKey);
        richTable.setSelectedRowKeys(newSet);
        richTable.setActiveRowKey(newSelectedRowKey);

        return newRow;
    }
    
    /**
      * 在拖动table的时候必须选择多选的table 返回选中多行的对象集
      * @param myTable
      * @return 选中的多行记录
      */
     public static List<Row> getTableSelectedRows(RichTable myTable) {
         List<Row> list = null;
         if (myTable == null) {
             return Collections.emptyList();
         } else {
             RowKeySet rowKeySet = myTable.getSelectedRowKeys();
             if (rowKeySet.isEmpty()) {
                 return Collections.emptyList();
             } else {
                 CollectionModel cm = (CollectionModel)myTable.getValue();
                 Object[] rowKeySetArray = rowKeySet.toArray();

                 list = new ArrayList();
                 for (Object facesTreeRowKey : rowKeySetArray) {
                     cm.setRowKey(facesTreeRowKey);
                     JUCtrlHierNodeBinding rowData =
                         (JUCtrlHierNodeBinding)cm.getRowData();
                     if (rowData != null) {
                         list.add(rowData.getRow());
                     }
                 }
                 return list;
             }
         }
     }

    
}