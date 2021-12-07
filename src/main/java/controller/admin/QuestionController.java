package controller.admin;

import converter.QuestionConverter;
import dto.ExamDTO;
import dto.QuestionDTO;
import entity.Exam;
import entity.Question;
import service.IQuestionService;
import service.impl.ExamService;
import util.FormUtil;

import javax.inject.Inject;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;


@MultipartConfig
@WebServlet(name = "QuestionController", value = "/QuestionController-file")
public class QuestionController extends HttpServlet {
    @Inject
    private IQuestionService questionService;
    @Inject
    private ExamService examService;

    private QuestionConverter questionConverter = new QuestionConverter();
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");
        System.out.println("get action");
        if(action.equals("insertFile")){
            System.out.println("check action");
            int id = Integer.parseInt(request.getParameter("id"));
            System.out.println("get id");
            int courseID = Integer.parseInt(request.getParameter("courseID"));
            System.out.println("get course id");
            String examName = request.getParameter("examName");
            System.out.println("get examName");
            String fileCheck = request.getParameter("fileCheck");
            System.out.println("get fileCheck");
            ExamDTO examDTO = new ExamDTO();
            System.out.println("new DTO");
            examDTO.setId(id);
            System.out.println("set id");
            examDTO.setExamName(examName);
            System.out.println("set examName");
            examDTO.setCourseID(courseID);
            System.out.println("set courseID");
            examDTO.setFileCheck(fileCheck);
            System.out.println("set fileCheck");
            try{

                Part part = request.getPart("file-exam");
                String realPath = request.getServletContext().getRealPath("/fileupload");
                String filename = Paths.get(part.getSubmittedFileName()).getFileName().toString();

                if(!Files.exists(Paths.get(realPath))){
                    Files.createDirectories(Paths.get(realPath));
                }
                String address = realPath + "/"+filename;
                part.write(address);

                String msg = questionService.importExcelXLSX(address, examDTO);
                if(msg != null){
                    Exam exam = examService.updateExam(examDTO);
                    response.sendRedirect(request.getContextPath()+"/admin-manager-file?message=insert_success");
                }
                else{
                    response.sendRedirect(request.getContextPath()+"/admin-manager-file?message=error_system");
                }
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }
        else {
            if (action.equals("deleteFile")){
                ExamDTO examDTO = FormUtil.toModel(ExamDTO.class, request);
                int examId = examDTO.getId();
                List<Question> list = questionService.getListQuestionByExamId(examId);

                int length = list.size();
                int count = 0;
                for (Question question : list){
                    QuestionDTO questionDTO = questionConverter.toDto(question);
                    questionService.deleteQuestion(questionDTO);
                    count = count + 1;
                }
                if (count == length){
                    if (examService.deleteExam(examDTO)){
                        response.sendRedirect(request.getContextPath()+"/admin-manager-file?message=delete_success");
                    }
                    else{
                        response.sendRedirect(request.getContextPath()+"/admin-manager-file?message=error_system");
                    }
                }
                else{
                    response.sendRedirect(request.getContextPath()+"/admin-manager-file?message=error_system");
                }
            }
        }
    }
}
