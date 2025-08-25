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

@Service
public class CreditItemService {
    private final CreditItemRepository creditItemRepository;
    private final StudentRepository studentRepository;
    private final StudentCreditRepository studentCreditRepository;

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
        // 每个类别仅允许一个配置项，确保与前端“德/智/体/美/劳分别配置”一致
        if (creditItemRepository.existsByCategory(dto.getCategory())) {
            throw new IllegalArgumentException("该类别已存在配置项");
        }
        Optional<CreditItem> exists = creditItemRepository.findByCategoryAndItemName(dto.getCategory(), dto.getItemName());
        if (exists.isPresent()) throw new IllegalArgumentException("同类目下的项目名称已存在");
        CreditItem item = new CreditItem();
        item.setCategory(dto.getCategory());
        item.setItemName(dto.getItemName());
        item.setInitialScore(dto.getInitialScore());
        item.setMaxScore(dto.getMaxScore());
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
        if (dto.getItemName() != null && !dto.getItemName().equals(item.getItemName())) {
            Optional<CreditItem> exists = creditItemRepository.findByCategoryAndItemName(item.getCategory(), dto.getItemName());
            if (exists.isPresent()) throw new IllegalArgumentException("同类目下的项目名称已存在");
            item.setItemName(dto.getItemName());
        }
        if (dto.getMaxScore() != null) item.setMaxScore(dto.getMaxScore());
        if (dto.getInitialScore() != null) item.setInitialScore(dto.getInitialScore());
        if (dto.getEnabled() != null) item.setEnabled(dto.getEnabled());
        if (dto.getDescription() != null) item.setDescription(dto.getDescription());
        return toDTO(creditItemRepository.save(item));
    }

    private CreditItemDTO toDTO(CreditItem item) {
        return new CreditItemDTO(item.getId(), item.getCategory(), item.getItemName(), item.getInitialScore(), item.getMaxScore(), item.getEnabled(), item.getDescription());
    }
}
