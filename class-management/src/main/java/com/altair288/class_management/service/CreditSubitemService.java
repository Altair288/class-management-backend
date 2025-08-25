package com.altair288.class_management.service;

import com.altair288.class_management.dto.CreditSubitemDTO;
import com.altair288.class_management.model.CreditItem;
import com.altair288.class_management.model.CreditSubitem;
import com.altair288.class_management.repository.CreditItemRepository;
import com.altair288.class_management.repository.CreditSubitemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CreditSubitemService {

    private final CreditSubitemRepository subitemRepository;
    private final CreditItemRepository itemRepository;

    public CreditSubitemService(CreditSubitemRepository subitemRepository,
                                CreditItemRepository itemRepository) {
        this.subitemRepository = subitemRepository;
        this.itemRepository = itemRepository;
    }

    public List<CreditSubitemDTO> listByItem(Integer itemId) {
        return subitemRepository.findByItemId(itemId).stream().map(this::toDTO).toList();
    }

    @Transactional
    public CreditSubitemDTO create(CreditSubitemDTO dto) {
        Integer itemId = req(dto.getItemId(), "itemId");
        String name = req(dto.getSubitemName(), "subitemName");
        itemRepository.findById(itemId).orElseThrow(() -> new IllegalArgumentException("主项目不存在"));
        subitemRepository.findByItemIdAndSubitemName(itemId, name).ifPresent(x -> {
            throw new IllegalArgumentException("该主项目下已存在同名子项目");
        });

        CreditSubitem s = new CreditSubitem();
        s.setItem(itemRepository.getReferenceById(itemId));
        s.setSubitemName(name);
        s.setInitialScore(nonNeg(dto.getInitialScore(), 0));
        s.setMaxScore(nonNeg(dto.getMaxScore(), 100));
        s.setWeight(nonNegD(dto.getWeight(), 0d));
        s.setEnabled(dto.getEnabled() == null ? Boolean.TRUE : dto.getEnabled());
        s = subitemRepository.save(s);
        return toDTO(s);
    }

    @Transactional
    public CreditSubitemDTO update(Integer id, CreditSubitemDTO dto) {
        CreditSubitem s = subitemRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("子项目不存在"));
        if (dto.getSubitemName() != null && !dto.getSubitemName().equals(s.getSubitemName())) {
            // 同父项下重名校验
            subitemRepository.findByItemIdAndSubitemName(s.getItem().getId(), dto.getSubitemName()).ifPresent(x -> {
                throw new IllegalArgumentException("该主项目下已存在同名子项目");
            });
            s.setSubitemName(dto.getSubitemName());
        }
        if (dto.getInitialScore() != null) s.setInitialScore(nonNeg(dto.getInitialScore(), 0));
        if (dto.getMaxScore() != null) s.setMaxScore(nonNeg(dto.getMaxScore(), 100));
        if (dto.getWeight() != null) s.setWeight(nonNegD(dto.getWeight(), 0d));
        if (dto.getEnabled() != null) s.setEnabled(dto.getEnabled());
        s = subitemRepository.save(s);
        return toDTO(s);
    }

    @Transactional
    public void delete(Integer id) {
        subitemRepository.deleteById(id);
    }

    private CreditSubitemDTO toDTO(CreditSubitem s) {
        return new CreditSubitemDTO(
                s.getId(),
                s.getItem().getId(),
                s.getSubitemName(),
                s.getInitialScore(),
                s.getMaxScore(),
                s.getWeight(),
                s.getEnabled()
        );
    }

    private <T> T req(T v, String name) {
        if (v == null || (v instanceof String str && str.isBlank()))
            throw new IllegalArgumentException(name + " 不能为空");
        return v;
    }
    private Integer nonNeg(Integer v, Integer def) { return v == null ? def : Math.max(0, v); }
    private Double nonNegD(Double v, Double def) { return v == null ? def : Math.max(0d, v); }
}