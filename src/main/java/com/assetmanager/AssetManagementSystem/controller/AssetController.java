package com.assetmanager.AssetManagementSystem.controller;

import com.assetmanager.AssetManagementSystem.dto.CreateAssetRequest;
import com.assetmanager.AssetManagementSystem.entity.Asset;
import com.assetmanager.AssetManagementSystem.service.AssetService;
import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/assets")
public class AssetController {

    private final AssetService assetService;

    @GetMapping
    public String listAssets(Model model) {

        model.addAttribute("assets", assetService.getAllAssets());

        return "assets/list";
    }

    @GetMapping("/create")
    public String createForm(Model model) {

        model.addAttribute("assetRequest", new CreateAssetRequest());

        return "assets/create";
    }

    @PostMapping("/create")
    public String createAsset(
            @Valid
            @ModelAttribute("assetRequest")
            CreateAssetRequest request,
            BindingResult result) {

        if (result.hasErrors()) {
            return "assets/create";
        }

        assetService.createAsset(request);

        return "redirect:/assets";
    }

    @GetMapping("/{id}")
    public String details(
            @PathVariable
            Long id,
            Model model) {

        model.addAttribute("asset", assetService.getAsset(id));

        return "assets/details";
    }

    @GetMapping("/{id}/edit")
    public String editForm(
            @PathVariable
            Long id,
            Model model) {

        Asset asset = assetService.getAsset(id);

        CreateAssetRequest request = new CreateAssetRequest();
        request.setTitle(asset.getTitle());
        request.setCategory(asset.getCategory());
        request.setSerialNumber(asset.getSerialNumber());
        request.setAcquisitionDate(asset.getAcquisitionDate());
        request.setCost(asset.getCost());
        request.setLocation(asset.getLocation());
        request.setCondition(asset.getCondition());

        model.addAttribute("assetRequest", request);
        model.addAttribute("assetId", id);

        return "assets/edit";
    }

    @PostMapping("/{id}/edit")
    public String editAsset(
            @PathVariable
            Long id,
            @Valid
            @ModelAttribute("assetRequest")
            CreateAssetRequest request,
            BindingResult result,
            Model model) {

        if (result.hasErrors()) {
            model.addAttribute("assetId", id);
            return "assets/edit";
        }

        assetService.updateAsset(id, request);

        return "redirect:/assets/" + id;
    }

    @PostMapping("/{id}/retire")
    public String retireAsset(
            @PathVariable
            Long id) {

        assetService.retireAsset(id);

        return "redirect:/assets";
    }

    @GetMapping("/search")
    public String search(
            @RequestParam
            String keyword,
            Model model) {

        model.addAttribute("assets", assetService.searchByTitle(keyword));
        model.addAttribute("keyword", keyword);

        return "assets/list";
    }

    @GetMapping("/report")
    public String inventoryReport(Model model) {

        model.addAttribute("assets", assetService.getAllAssets());

        return "reports/inventory";
    }
}
