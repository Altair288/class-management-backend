package com.altair288.class_management.service;

import com.altair288.class_management.dto.StudentCreditItemDTO;
import com.altair288.class_management.dto.StudentCreditsDTO;
import com.altair288.class_management.model.CreditItem;
import com.altair288.class_management.model.Student;
import com.altair288.class_management.model.StudentCredit;
import com.altair288.class_management.repository.CreditItemRepository;
import com.altair288.class_management.repository.StudentCreditRepository;
import com.altair288.class_management.repository.StudentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class StudentCreditService {
    private final StudentRepository studentRepository;
    private final StudentCreditRepository studentCreditRepository;
    private final CreditItemRepository creditItemRepository;

    public StudentCreditService(StudentRepository studentRepository, StudentCreditRepository studentCreditRepository, CreditItemRepository creditItemRepository) {
        this.studentRepository = studentRepository;
        this.studentCreditRepository = studentCreditRepository;
        this.creditItemRepository = creditItemRepository;
    }

    public StudentCreditsDTO getTotalsForStudent(Integer studentId) {
        Student s = studentRepository.findById(studentId).orElseThrow(() -> new IllegalArgumentException("学生不存在"));
        double de = sum(studentId, "德");
        double zhi = sum(studentId, "智");
        double ti = sum(studentId, "体");
        double mei = sum(studentId, "美");
        double lao = sum(studentId, "劳");
        return new StudentCreditsDTO(s.getId(), s.getName(), s.getStudentNo(), de, zhi, ti, mei, lao);
    }

    public List<StudentCreditsDTO> getTotalsForClass(Integer classId) {
        List<Student> students = studentRepository.findByClazzId(classId);
        List<StudentCreditsDTO> list = new ArrayList<>();
        for (Student s : students) {
            StudentCreditsDTO dto = getTotalsForStudent(s.getId());
            list.add(dto);
        }
        return list;
    }

    @Transactional
    public void updateScore(Integer studentId, Integer creditItemId, Double delta) {
        if (delta == null || Objects.equals(delta, 0.0)) return;
        StudentCredit sc = studentCreditRepository.findByStudentAndItem(studentId, creditItemId);
        if (sc == null) {
            Student s = studentRepository.findById(studentId).orElseThrow(() -> new IllegalArgumentException("学生不存在"));
            CreditItem item = creditItemRepository.findById(creditItemId).orElseThrow(() -> new IllegalArgumentException("项目不存在"));
            sc = new StudentCredit();
            sc.setStudent(s);
            sc.setCreditItem(item);
            sc.setScore(0.0);
        }
        double newScore = sc.getScore() + delta;
        Double max = sc.getCreditItem().getMaxScore();
        if (max != null && newScore > max) newScore = max;
        if (newScore < 0) newScore = 0;
        sc.setScore(newScore);
        studentCreditRepository.save(sc);
    }

    public List<StudentCreditItemDTO> listStudentItems(Integer studentId, String category) {
        // ensure student exists
        studentRepository.findById(studentId).orElseThrow(() -> new IllegalArgumentException("学生不存在"));
        List<StudentCredit> list = studentCreditRepository.findByStudentAndOptionalCategory(studentId, category);
        List<StudentCreditItemDTO> dtoList = new ArrayList<>();
        for (StudentCredit sc : list) {
            CreditItem item = sc.getCreditItem();
            dtoList.add(new StudentCreditItemDTO(
                    item.getId(),
                    item.getCategory(),
                    item.getItemName(),
                    sc.getScore(),
                    item.getMaxScore(),
                    item.getEnabled(),
                    item.getDescription()
            ));
        }
        return dtoList;
    }

    private double sum(Integer studentId, String category) {
        Double v = studentCreditRepository.sumScoreByStudentAndCategory(studentId, category);
        return v == null ? 0.0 : v;
    }

    @Transactional
    public void setScore(Integer studentId, Integer creditItemId, Double value) {
        if (value == null) return;
        StudentCredit sc = studentCreditRepository.findByStudentAndItem(studentId, creditItemId);
        if (sc == null) {
            Student s = studentRepository.findById(studentId).orElseThrow(() -> new IllegalArgumentException("学生不存在"));
            CreditItem item = creditItemRepository.findById(creditItemId).orElseThrow(() -> new IllegalArgumentException("项目不存在"));
            sc = new StudentCredit();
            sc.setStudent(s);
            sc.setCreditItem(item);
        }
        double newScore = value;
        Double max = sc.getCreditItem().getMaxScore();
        if (max != null && newScore > max) newScore = max;
        if (newScore < 0) newScore = 0;
        sc.setScore(newScore);
        studentCreditRepository.save(sc);
    }

    /**
     * 将某个主项目的规则应用给所有学生。
     * mode:
     *  - reset: 所有学生该项目的分数重置为 item.initialScore（默认）。
     *  - clamp: 只按 item.maxScore 进行封顶（若降低了最大分），不改变其他分数。
     * 返回受影响记录数。
     */
    @Transactional
    public int applyItemRule(Integer itemId, String mode) {
        CreditItem item = creditItemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("项目不存在"));
        String m = (mode == null || mode.isBlank()) ? "reset" : mode.trim().toLowerCase();
        List<StudentCredit> list = studentCreditRepository.findAllByCreditItem_Id(itemId);
        int affected = 0;
        if ("reset".equals(m)) {
            double init = item.getInitialScore() == null ? 0.0 : item.getInitialScore();
            for (StudentCredit sc : list) {
                double v = init;
                Double max = item.getMaxScore();
                if (max != null && v > max) v = max;
                if (v < 0) v = 0;
                if (!Objects.equals(sc.getScore(), v)) {
                    sc.setScore(v);
                    affected++;
                }
            }
            studentCreditRepository.saveAll(list);
            return affected;
        } else if ("clamp".equals(m)) {
            Double max = item.getMaxScore();
            if (max == null) return 0;
            for (StudentCredit sc : list) {
                if (sc.getScore() != null && sc.getScore() > max) {
                    sc.setScore(max);
                    affected++;
                }
            }
            studentCreditRepository.saveAll(list);
            return affected;
        } else {
            throw new IllegalArgumentException("不支持的模式: " + mode + "，可选 reset/clamp");
        }
    }
}
