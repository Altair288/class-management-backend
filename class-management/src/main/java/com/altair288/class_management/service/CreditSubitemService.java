package com.altair288.class_management.service;

import com.altair288.class_management.dto.CreditSubitemDTO;
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

    public CreditSubitemDTO getById(Integer id) {
        CreditSubitem s = subitemRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("子项目不存在"));
        return toDTO(s);
    }

    @Transactional
    public CreditSubitemDTO create(CreditSubitemDTO dto) {
        Integer itemId = req(dto.getItemId(), "itemId");
        String name = req(dto.getSubitemName(), "subitemName");
        itemRepository.findById(itemId).orElseThrow(() -> new IllegalArgumentException("主项目不存在"));
        if (subitemRepository.findByItemIdAndSubitemName(itemId, name).isPresent()) {
            throw new IllegalArgumentException("该主项目下已存在同名子项目");
        }

        // 范围校验：分值 0-100，权重 0-1
        int init = range0to100(dto.getInitialScore(), 0);
        int max = range0to100(dto.getMaxScore(), 100);
        if (init > max) throw new IllegalArgumentException("子项目初始分不能大于最大分");
        double weight = range0to1(dto.getWeight(), 0d);

        CreditSubitem s = new CreditSubitem();
        s.setItem(itemRepository.getReferenceById(itemId));
        s.setSubitemName(name);
        s.setInitialScore(init);
        s.setMaxScore(max);
        s.setWeight(weight);
        s.setEnabled(dto.getEnabled() == null ? Boolean.TRUE : dto.getEnabled());
        s = subitemRepository.save(s);

        // 若启用，校验启用子项的权重和不超过 1.0
        if (s.getEnabled()) {
            Double sum = subitemRepository.sumEnabledWeightByItem(itemId);
            if (sum != null && sum > 1.00001) {
                throw new IllegalArgumentException("该主项目下启用子项权重之和不能超过 1.0");
            }
        }
        return toDTO(s);
    }

    @Transactional
    public CreditSubitemDTO update(Integer id, CreditSubitemDTO dto) {
        CreditSubitem s = subitemRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("子项目不存在"));
        if (dto.getSubitemName() != null && !dto.getSubitemName().equals(s.getSubitemName())) {
            // 同父项下重名校验
            if (subitemRepository.findByItemIdAndSubitemName(s.getItem().getId(), dto.getSubitemName()).isPresent()) {
                throw new IllegalArgumentException("该主项目下已存在同名子项目");
            }
            s.setSubitemName(dto.getSubitemName());
        }
        Integer init = dto.getInitialScore();
        Integer max = dto.getMaxScore();
        Double weight = dto.getWeight();
        if (init != null) init = range0to100(init, 0);
        if (max != null) max = range0to100(max, 100);
        if (init != null && max != null && init > max) throw new IllegalArgumentException("子项目初始分不能大于最大分");
        if (weight != null) weight = range0to1(weight, 0d);
        if (init != null) s.setInitialScore(init);
        if (max != null) s.setMaxScore(max);
        if (weight != null) s.setWeight(weight);
        if (dto.getEnabled() != null) s.setEnabled(dto.getEnabled());
        s = subitemRepository.save(s);

        // 若启用，校验启用子项的权重和不超过 1.0
        if (s.getEnabled()) {
            Double sum = subitemRepository.sumEnabledWeightByItem(s.getItem().getId());
            if (sum != null && sum > 1.00001) {
                throw new IllegalArgumentException("该主项目下启用子项权重之和不能超过 1.0");
            }
        }
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
    private Integer range0to100(Integer v, Integer def) { v = (v == null ? def : v); if (v < 0) v = 0; if (v > 100) v = 100; return v; }
    private Double range0to1(Double v, Double def) { v = (v == null ? def : v); if (v < 0d) v = 0d; if (v > 1d) v = 1d; return v; }
}