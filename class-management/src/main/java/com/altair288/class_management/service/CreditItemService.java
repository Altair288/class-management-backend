package com.altair288.class_management.service;

import com.altair288.class_management.dto.CreditItemDTO;
import com.altair288.class_management.model.CreditItem;
import com.altair288.class_management.model.Student;
import com.altair288.class_management.model.StudentCredit;
import com.altair288.class_management.repository.CreditItemRepository;
import com.altair288.class_management.repository.StudentCreditRepository;
import com.altair288.class_management.repository.StudentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.Set;

@Service
public class CreditItemService {
    private final CreditItemRepository creditItemRepository;
    private final StudentRepository studentRepository;
    private final StudentCreditRepository studentCreditRepository;
    private static final Set<String> ALLOWED_CATEGORIES = Set.of("德","智","体","美","劳");

    public CreditItemService(CreditItemRepository creditItemRepository, StudentRepository studentRepository, StudentCreditRepository studentCreditRepository) {
        this.creditItemRepository = creditItemRepository;
        this.studentRepository = studentRepository;
        this.studentCreditRepository = studentCreditRepository;
    }

    public List<CreditItemDTO> list(String category) {
        List<CreditItem> items = creditItemRepository.findByCategoryOptional(category);
        return items.stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Transactional
    public CreditItemDTO create(CreditItemDTO dto) {
        String category = req(dto.getCategory(), "category");
        String itemName = req(dto.getItemName(), "itemName");
        Double initial = num(dto.getInitialScore(), 0d);
        Double max = num(dto.getMaxScore(), 100d);
        validateCategory(category);
        validateScores(initial, max, true);
        // 每个类别仅允许一个配置项
        if (creditItemRepository.existsByCategory(category)) {
            throw new IllegalArgumentException("该类别已存在配置项");
        }
        // 主项目名称不能重复（全局唯一）
        if (creditItemRepository.existsByItemName(itemName)) {
            throw new IllegalArgumentException("主项目名称已存在");
        }
        Optional<CreditItem> exists = creditItemRepository.findByCategoryAndItemName(category, itemName);
        if (exists.isPresent()) throw new IllegalArgumentException("同类目下的项目名称已存在");
        CreditItem item = new CreditItem();
        item.setCategory(category);
        item.setItemName(itemName);
        item.setInitialScore(initial);
        item.setMaxScore(max);
        item.setEnabled(dto.getEnabled() != null ? dto.getEnabled() : true);
        item.setDescription(dto.getDescription());
        CreditItem saved = creditItemRepository.save(item);

        // 初始化每个学生的 student_credit 记录
        Double init = saved.getInitialScore() != null ? saved.getInitialScore() : 0.0;
        List<Student> students = studentRepository.findAll();
        for (Student s : students) {
            StudentCredit sc = new StudentCredit();
            sc.setStudent(s);
            sc.setCreditItem(saved);
            sc.setScore(init);
            studentCreditRepository.save(sc);
        }

        return toDTO(saved);
    }

    @Transactional
    public CreditItemDTO update(Integer id, CreditItemDTO dto) {
        CreditItem item = creditItemRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("项目不存在"));
        if (dto.getCategory() != null) {
            validateCategory(dto.getCategory());
            if (!dto.getCategory().equals(item.getCategory())) {
                if (creditItemRepository.existsByCategory(dto.getCategory())) {
                    throw new IllegalArgumentException("该类别已存在配置项");
                }
                item.setCategory(dto.getCategory());
            }
        }
        if (dto.getItemName() != null && !dto.getItemName().equals(item.getItemName())) {
            if (creditItemRepository.existsByItemName(dto.getItemName())) {
                throw new IllegalArgumentException("主项目名称已存在");
            }
            Optional<CreditItem> exists = creditItemRepository.findByCategoryAndItemName(item.getCategory(), dto.getItemName());
            if (exists.isPresent()) throw new IllegalArgumentException("同类目下的项目名称已存在");
            item.setItemName(dto.getItemName());
        }
        Double newMax = dto.getMaxScore();
        Double newInit = dto.getInitialScore();
        Double finalMax = newMax != null ? newMax : item.getMaxScore();
        Double finalInit = newInit != null ? newInit : item.getInitialScore();
        validateScores(finalInit, finalMax, false);
        if (newMax != null) item.setMaxScore(finalMax);
        if (newInit != null) item.setInitialScore(finalInit);
        if (dto.getEnabled() != null) item.setEnabled(dto.getEnabled());
        if (dto.getDescription() != null) item.setDescription(dto.getDescription());
        return toDTO(creditItemRepository.save(item));
    }

    private CreditItemDTO toDTO(CreditItem item) {
        return new CreditItemDTO(item.getId(), item.getCategory(), item.getItemName(), item.getInitialScore(), item.getMaxScore(), item.getEnabled(), item.getDescription());
    }

    private String req(String v, String name) {
        if (v == null || v.isBlank()) throw new IllegalArgumentException(name + " 不能为空");
        return v;
    }

    private Double num(Double v, Double def) { return v == null ? def : v; }

    private void validateCategory(String category) {
        if (!ALLOWED_CATEGORIES.contains(category)) {
            throw new IllegalArgumentException("类别必须为：德/智/体/美/劳");
        }
    }

    private void validateScores(Double initial, Double max, boolean onCreate) {
        if (initial == null || max == null) throw new IllegalArgumentException("分值不能为空");
        if (initial < 0 || initial > 100) throw new IllegalArgumentException("初始分必须在 0-100 之间");
        if (max < 0 || max > 100) throw new IllegalArgumentException("最大分必须在 0-100 之间");
        if (initial > max) throw new IllegalArgumentException("初始分不能大于最大分");
    }
}
