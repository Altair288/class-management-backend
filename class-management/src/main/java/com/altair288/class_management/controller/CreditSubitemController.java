package com.altair288.class_management.controller;

import com.altair288.class_management.dto.CreditSubitemDTO;
import com.altair288.class_management.service.CreditSubitemService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/credits")
public class CreditSubitemController {

    private final CreditSubitemService subitemService;

    public CreditSubitemController(CreditSubitemService subitemService) {
        this.subitemService = subitemService;
    }

    // 新路径：按主项目列出子项目
    // GET /api/credits/items/{itemId}/subitems
    @GetMapping("/items/{itemId}/subitems")
    public ResponseEntity<List<CreditSubitemDTO>> listByItem(@PathVariable Integer itemId) {
        return ResponseEntity.ok(subitemService.listByItem(itemId));
    }

    // 兼容旧用法：/api/credits/subitems?itemId=xxx
    @GetMapping("/subitems")
    public ResponseEntity<List<CreditSubitemDTO>> listCompat(@RequestParam Integer itemId) {
        return ResponseEntity.ok(subitemService.listByItem(itemId));
    }

    // 新路径：在指定主项目下创建子项目
    // POST /api/credits/items/{itemId}/subitems
    @PostMapping("/items/{itemId}/subitems")
    public ResponseEntity<CreditSubitemDTO> create(@PathVariable Integer itemId, @RequestBody CreditSubitemDTO dto) {
        dto.setItemId(itemId); // 以路径为准
        return ResponseEntity.ok(subitemService.create(dto));
    }

    // 查询单个子项目
    // GET /api/credits/subitems/{id}
    @GetMapping("/subitems/{id}")
    public ResponseEntity<CreditSubitemDTO> getOne(@PathVariable Integer id) {
    return ResponseEntity.ok(subitemService.getById(id));
    }

    // 更新（与现有前端风格一致，用 POST）
    // POST /api/credits/subitems/{id}
    @PostMapping("/subitems/{id}")
    public ResponseEntity<CreditSubitemDTO> update(@PathVariable Integer id, @RequestBody CreditSubitemDTO dto) {
        return ResponseEntity.ok(subitemService.update(id, dto));
    }

    // 删除
    // DELETE /api/credits/subitems/{id}
    @DeleteMapping("/subitems/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        subitemService.delete(id);
        return ResponseEntity.ok().build();
    }
}