package com.altair288.class_management.service;

import org.apache.poi.ss.usermodel.*;
import org.springframework.web.multipart.MultipartFile;
import com.altair288.class_management.model.Class;
import com.altair288.class_management.model.Student;
import com.altair288.class_management.model.User;
import com.altair288.class_management.service.ClassService;
import com.altair288.class_management.service.UserService;
import com.altair288.class_management.model.Student;
import com.altair288.class_management.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class StudentService {
    @Autowired
    private StudentRepository studentRepository;
    @Autowired
    private ClassService classService;
    @Autowired
    private UserService userService;

    public void importStudentsFromExcel(Integer classId, MultipartFile file) throws Exception {
        Class clazz = classService.getById(classId);
        if (clazz == null)
            throw new IllegalArgumentException("班级不存在");

        Workbook workbook = WorkbookFactory.create(file.getInputStream());
        Sheet sheet = workbook.getSheetAt(0);

        int rowNum = 0;
        int studentSeq = getMaxStudentSeq(clazz); // 获取当前班级已有最大序号
        for (Row row : sheet) {
            if (rowNum++ == 0)
                continue; // 跳过表头

            String name = getCellString(row.getCell(0));
            String studentNo = getCellString(row.getCell(1));
            String phone = getCellString(row.getCell(2));
            String email = getCellString(row.getCell(3));

            // 自动生成学号
            if (studentNo == null || studentNo.isBlank()) {
                studentSeq++;
                studentNo = generateStudentNo(clazz, studentSeq);
            }

            // 检查学号唯一
            if (studentRepository.findByStudentNo(studentNo).isPresent()) {
                continue; // 或收集错误信息
            }

            // 创建学生
            Student student = new Student();
            student.setName(name);
            student.setPhone(phone);
            student.setEmail(email);
            student.setStudentNo(studentNo);
            student.setClazz(clazz);
            student = studentRepository.save(student);

            // 创建用户
            User user = new User();
            user.setUsername(name);
            user.setIdentityNo(studentNo);
            user.setPassword("Sgz@" + studentNo); // 初始密码
            user.setUserType(User.UserType.STUDENT);
            user.setRelatedId(student.getId());
            userService.registerUser(user);
        }
        workbook.close();
    }

    private String getCellString(Cell cell) {
        if (cell == null)
            return null;
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    double d = cell.getNumericCellValue();
                    if (d == (long) d) {
                        return String.valueOf((long) d);
                    } else {
                        return String.valueOf(d);
                    }
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                try {
                    return cell.getStringCellValue().trim();
                } catch (IllegalStateException e) {
                    double d = cell.getNumericCellValue();
                    if (d == (long) d) {
                        return String.valueOf((long) d);
                    } else {
                        return String.valueOf(d);
                    }
                }
            case BLANK:
                return "";
            default:
                return "";
        }
    }

    // 获取当前班级最大序号（如已有2400101、2400102，则返回2）
    private int getMaxStudentSeq(Class clazz) {
        String grade = clazz.getGrade(); // 如"24"
        String classNo = String.format("%03d", clazz.getId()); // 假设班级id就是班号
        List<Student> students = studentRepository.findByClazzId(clazz.getId());
        int max = 0;
        for (Student s : students) {
            String no = s.getStudentNo();
            if (no != null && no.startsWith(grade + classNo) && no.length() >= 7) {
                try {
                    int seq = Integer.parseInt(no.substring(5, 7));
                    if (seq > max)
                        max = seq;
                } catch (Exception ignored) {
                }
            }
        }
        return max;
    }

    // 生成学号：年级(2位)+班级编号(3位)+学生序号(2位)
    private String generateStudentNo(Class clazz, int seq) {
        String grade = clazz.getGrade(); // 如"24"
        String classNo = String.format("%03d", clazz.getId()); // 假设班级id就是班号
        return grade + classNo + String.format("%02d", seq);
    }

    public Student getStudentById(Integer id) {
        return studentRepository.findById(id).orElse(null);
    }

    public Student getStudentByStudentNo(String studentNo) {
        return studentRepository.findByStudentNo(studentNo)
                .orElseThrow(() -> new IllegalArgumentException("未找到该学号对应的学生"));
    }

    public Student save(Student student) {
        // Implement the logic to save the teacher entity
        // For example, if using JPA:
        return studentRepository.save(student);
    }

    public Long count() {
        return studentRepository.count();
    }

    public Long countByClassId(Integer classId) {
        return studentRepository.countByClazzId(classId);
    }

    public List<Student> findByClassId(Integer classId) {
        return studentRepository.findByClazzId(classId);
    }
}
