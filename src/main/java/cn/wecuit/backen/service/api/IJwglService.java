package cn.wecuit.backen.service.api;

import org.apache.hc.core5.http.ParseException;

import java.io.IOException;

public interface IJwglService {
    void loginAction() throws IOException, ParseException;
    void loginCheckAction() throws IOException, ParseException;
    void getGradeTableV2Action() throws IOException, ParseException;
    void getExamOptionAction() throws IOException, ParseException;
    void getExamTableAction() throws IOException, ParseException;
}
