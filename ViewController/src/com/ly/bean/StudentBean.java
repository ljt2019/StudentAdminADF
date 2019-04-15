package com.ly.bean;

import com.ly.model.am.StudentAdminAMImpl;
import com.ly.utils.ADFUtils;

import com.ly.utils.JSFUtils;

import javax.faces.component.UIComponent;

import oracle.adf.model.BindingContainer;
import oracle.adf.model.BindingContext;
import oracle.adf.share.logging.ADFLogger;
import oracle.adf.view.rich.component.rich.RichPopup;
import oracle.adf.view.rich.event.DialogEvent;
import oracle.adf.view.rich.event.PopupCanceledEvent;

import oracle.jbo.ApplicationModule;
import oracle.jbo.Row;
import oracle.jbo.RowSetIterator;
import oracle.jbo.ViewCriteria;
import oracle.jbo.ViewObject;
import oracle.jbo.server.ApplicationModuleImpl;
import oracle.jbo.server.ViewObjectImpl;

public class StudentBean {

    public static final ADFLogger LOGGER =
        ADFLogger.createADFLogger(StudentBean.class);

    public StudentBean() {
    }


    /**
     *添加学生信息，提交事务Commit
     * @param dialogEvent
     */
    public void addStudent(DialogEvent dialogEvent) {
        LOGGER.info("========添加学生信息=======");
        if ("ok".equals(dialogEvent.getOutcome().name())) {
            ADFUtils.executeByOperation("Commit");
            LOGGER.info("========添加学生信息成功，刷新页面=======");
            ADFUtils.refresh(JSFUtils.findComponentInRoot("t1"));
        }
    }

    /**
     *取消添加
     * @param popupCanceledEvent
     */
    public void cancel(PopupCanceledEvent popupCanceledEvent) {
        LOGGER.info("========取消,回滚=======");
        //        ViewObjectImpl studentInfo = (ViewObjectImpl)ADFUtils.findIterator("XsxxglXsjbxxView1Iterator").getViewObject();
        //        studentInfo.getDBTransaction().rollback();
        ADFUtils.executeByOperation("Rollback");

    }


    /**
     *显示弹出框
     * @return
     */
    public String showPopup() {
        LOGGER.info("========显示弹出框=======");
        //通过学生VO迭代器获取学生VO实体类
        ViewObject studentInfo =
            ADFUtils.findIterator("XsxxglXsjbxxView1Iterator").getViewObject();
        Row row = studentInfo.createRow(); //创建一行
        studentInfo.insertRow(row); //插入一行空行
        UIComponent ui = JSFUtils.findComponentInRoot("p1"); //这里的p1是页面中弹窗的id
        ADFUtils.showPopup((RichPopup)ui); //在弹出框中输入信息最终提交数据

        return null;
    }

    /**
     *删除学生信息
     * @return
     */
    public String deleteStudent() {
        LOGGER.info("========删除学生信息=======");

        //        ViewObject studentInfo =
        //            ADFUtils.findIterator("XsxxglXsjbxxView1Iterator").getViewObject();
        //        studentInfo.removeCurrentRow();

        Row row =
            ADFUtils.findIterator("XsxxglXsjbxxView1Iterator").getCurrentRow();
        row.remove(); //移除当前行

        ADFUtils.executeByOperation("Commit"); //提交事务
        ADFUtils.refresh(JSFUtils.findComponentInRoot("t1")); //刷新表的
        return null;
    }

    /**
     *测试条件查询
     * @return
     */
    public String criteria() {
        LOGGER.info("========测试条件查询=======");
        //        //获取到学生对象
        //        ViewObject student = ADFUtils.findIterator("XsxxglXsjbxxView1Iterator").getViewObject();
        //        student.setWhereClause("Bjdm='23'");
        //        student.executeQuery();
        //        LOGGER.info("========查询语句======="+student.getQuery());
        //        LOGGER.info("========查询结果总数======="+student.getEstimatedRowCount());

        //绑定变量查询
        ViewObject student =
            ADFUtils.findIterator("XsxxglXsjbxxView1Iterator").getViewObject();

        student.defineNamedWhereClauseParam("p_studentID", null, null);
        student.setWhereClause("Xsid=:p_studentID");
        student.setNamedWhereClauseParam("p_studentID", "20150340830");
        student.executeQuery(); //执行查询
        LOGGER.info("========绑定查询(效率高)语句=======" + student.getQuery());
        LOGGER.info("========查询结果总数====== " + student.getEstimatedRowCount());

        //获取迭代器迭代查询出的数据
        RowSetIterator iter = student.createRowSetIterator("temmp");

        student.removeNamedWhereClauseParam("p_studentID");
        LOGGER.info("========删除绑定变量=======");
        return null;
    }

    /**
     *获取AM
     * @return
     */
    public String getAM() {
        LOGGER.info("========获取AM 方式1,通过vo获取=======");
        ViewObject student =
            ADFUtils.findIterator("XsxxglXsjbxxView1Iterator").getViewObject();
        StudentAdminAMImpl am1 =
            (StudentAdminAMImpl)student.getApplicationModule();
        LOGGER.info("========am 方式1======= " + am1);
        ViewObjectImpl major = am1.getDmjZyxxView1();
        LOGGER.info("========major 方式1======= " + major.getName());

        LOGGER.info("========获取AM 方式2，通过 Data Controls 名称获取=======");
        StudentAdminAMImpl am2 =
            (StudentAdminAMImpl)ADFUtils.getApplicationModuleForDataControl("StudentAdminAMDataControl");
        LOGGER.info("========am 方式2======= " + am2);
        ViewObjectImpl object = am2.getDmjZyxxView1();

        //两种方式获取到的是同一个对象，地址相同，推荐使用第一种
        return null;
    }

    /**
     *更换查询条件1
     * @return
     */
    public String changeCriteriaFirst() {
        LOGGER.info("======== 更换查询条件1 ======= ");
        ViewObjectImpl student =
            (ViewObjectImpl)ADFUtils.findIterator("XsxxglXsjbxxView1Iterator").getViewObject();
        ViewCriteria vc = student.getViewCriteria("changeCriteriaFirst");
        student.clearViewCriterias();
        student.applyViewCriteria(vc);
        student.executeQuery();
        //刷新页面
        ADFUtils.refresh(JSFUtils.findComponentInRoot("t1")); //刷新表的
        return null;
    }

}
