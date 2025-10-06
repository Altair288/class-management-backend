package com.altair288.class_management.service;

import org.apache.poi.ss.usermodel.*;
import org.springframework.web.multipart.MultipartFile;
import com.altair288.class_management.model.Class;
import com.altair288.class_management.model.Student;
import com.altair288.class_management.model.User;
import com.altair288.class_management.model.CreditItem;
import com.altair288.class_management.model.StudentCredit;
import com.altair288.class_management.repository.StudentRepository;
import com.altair288.class_management.repository.CreditItemRepository;
import com.altair288.class_management.repository.StudentCreditRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.altair288.class_management.dto.ImportStudentsResult;
import java.util.*;

@Service
public class StudentService {
    @Autowired
    private StudentRepository studentRepository;
    @Autowired
    private ClassService classService;
    @Autowired
    private UserService userService;
    @Autowired
    private CreditItemRepository creditItemRepository;
    @Autowired
    private StudentCreditRepository studentCreditRepository;
    @Autowired
    private StudentEvaluationService studentEvaluationService;

    @Autowired
    private StudentLeaveBalanceService studentLeaveBalanceService;

    @Transactional
    public ImportStudentsResult importStudentsFromExcel(Integer classId, MultipartFile file) throws Exception {
        Class clazz = classService.getById(classId);
        if (clazz == null)
            throw new IllegalArgumentException("班级不存在");

        Workbook workbook = WorkbookFactory.create(file.getInputStream());
        Sheet sheet = workbook.getSheetAt(0);
        ImportStudentsResult result = new ImportStudentsResult();
        int rowNum = 0; // 真实 Excel 行号（0 为表头）
        int studentSeq = getMaxStudentSeq(clazz); // 当前已有最大序号
        for (Row row : sheet) {
            if (rowNum == 0) { // 表头
                rowNum++;
                continue;
            }
            // 读取
            String nameRaw = getCellString(row.getCell(0));
            String studentNoRaw = getCellString(row.getCell(1));
            String phoneRaw = getCellString(row.getCell(2));
            String emailRaw = getCellString(row.getCell(3));

            // 判断是否全空
            boolean allBlank = isBlank(nameRaw) && isBlank(studentNoRaw) && isBlank(phoneRaw) && isBlank(emailRaw);
            if (allBlank) { rowNum++; continue; }
            result.incProcessed();

            // 姓名校验（非空 & 非空白）
            if (isBlank(nameRaw)) {
                result.addRowError(rowNum+1, "姓名不能为空"); // +1 使其与 Excel 视觉行号对齐
                rowNum++;
                continue;
            }
            String name = nameRaw.trim();

            // 学号处理
            String studentNo = (studentNoRaw == null || studentNoRaw.isBlank()) ? null : studentNoRaw.trim();
            if (studentNo == null) {
                studentSeq++;
                studentNo = generateStudentNo(clazz, studentSeq);
            }

            // 学号唯一检查
            if (studentRepository.findByStudentNo(studentNo).isPresent()) {
                result.addDuplicate(studentNo);
                rowNum++;
                continue;
            }

            // 可选字段规范化：空白 -> null
            String phone = normalizeOptional(phoneRaw);
            String email = normalizeOptional(emailRaw);

            // 唯一性（若填）预检查；忽略大小写可在此增强
            if (phone != null && studentRepository.findByPhone(phone).isPresent()) {
                result.addRowError(rowNum+1, "手机号重复:" + phone);
                rowNum++;
                continue;
            }
            if (email != null && studentRepository.findByEmail(email).isPresent()) {
                result.addRowError(rowNum+1, "邮箱重复:" + email);
                rowNum++;
                continue;
            }

            // 创建学生记录
            Student student = new Student();
            student.setName(name);
            student.setPhone(phone);
            student.setEmail(email);
            student.setStudentNo(studentNo);
            student.setClazz(clazz);
            student = this.save(student);

            // 用户（使用学号作 username 更稳定；显示昵称使用 Student.name）
            User user = new User();
            // 使用学号作为 username，确保姓名允许重复不会触发 user.username 唯一约束
            user.setUsername(studentNo);
            user.setIdentityNo(studentNo);
            user.setPassword("Sgz@" + studentNo);
            user.setUserType(User.UserType.STUDENT);
            user.setRelatedId(student.getId());
            userService.registerUser(user);

            result.incSuccess();
            rowNum++;
        }
        result.setTotalRows(rowNum); // 表头 + 数据处理过的行数
        workbook.close();
        return result;
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

    private boolean isBlank(String s){ return s == null || s.trim().isEmpty(); }
    private String normalizeOptional(String s){ return isBlank(s)? null : s.trim(); }

    @Transactional
    public Student save(Student student) {
        boolean isNew = (student.getId() == null);
        Student saved = studentRepository.save(student);
        // 新增学生时，为所有已存在的主项目初始化学生学分记录
        if (isNew) {
            List<CreditItem> items = creditItemRepository.findAll();
            for (CreditItem item : items) {
                StudentCredit sc = new StudentCredit();
                sc.setStudent(saved);
                sc.setCreditItem(item);
                sc.setScore(item.getInitialScore() == null ? 0.0 : item.getInitialScore());
                studentCreditRepository.save(sc);
            }
            // 初始化完项目得分后，计算并落库总分与等级
            try { studentEvaluationService.recomputeForStudent(saved.getId()); } catch (Exception ignored) {}

            // 新增学生时，同步初始化当年所有启用请假类型的余额
            try {
                Integer year = Calendar.getInstance().get(Calendar.YEAR);
                studentLeaveBalanceService.initializeBalancesForStudentAllEnabled(saved.getId(), year);
            } catch (Exception ignored) {}
        }
        return saved;
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
