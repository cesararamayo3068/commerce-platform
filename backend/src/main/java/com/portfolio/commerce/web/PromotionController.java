package com.portfolio.commerce.web;
import com.portfolio.commerce.service.PromotionService;
import com.portfolio.commerce.web.dto.*;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController @RequestMapping("/api/promotions")
public class PromotionController {
 private final PromotionService service;
 public PromotionController(PromotionService service){this.service=service;}
 @GetMapping public List<PromotionResponse> list(){return service.list();}
 @PostMapping public ResponseEntity<PromotionResponse> create(@Valid @RequestBody PromotionCreateRequest request){return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));}
 @PutMapping("/{id}/deactivate") public PromotionResponse deactivate(@PathVariable Long id){return service.deactivate(id);}
}
