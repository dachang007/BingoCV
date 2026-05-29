package net.de5.yeoh.bingocv.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import net.de5.yeoh.bingocv.common.annotation.CheckLogin;
import net.de5.yeoh.bingocv.common.api.Result;
import net.de5.yeoh.bingocv.common.enums.InfoEnum;
import net.de5.yeoh.bingocv.common.exception.InfoException;
import net.de5.yeoh.bingocv.common.utils.mvc.UserContext;
import net.de5.yeoh.bingocv.model.dto.ShareAccessRequest;
import net.de5.yeoh.bingocv.model.dto.ShareCreateRequest;
import net.de5.yeoh.bingocv.model.dto.ShareUpdateRequest;
import net.de5.yeoh.bingocv.model.vo.PublicResumeVO;
import net.de5.yeoh.bingocv.model.vo.ShareAccessStatsVO;
import net.de5.yeoh.bingocv.model.vo.ShareVO;
import net.de5.yeoh.bingocv.service.ShareService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@Tag(name = "Resume sharing")
public class ShareController {

    @Autowired
    private ShareService shareService;

    @GetMapping("/share/list")
    @CheckLogin
    @Operation(summary = "List my resume shares")
    public Result<List<ShareVO>> list() {
        return Result.ok(shareService.myShares(requireUserId()));
    }

    @PostMapping("/share")
    @CheckLogin
    @Operation(summary = "Create resume share")
    public Result<ShareVO> create(@RequestBody ShareCreateRequest request) {
        return Result.ok(shareService.createShare(requireUserId(), request));
    }

    @PutMapping("/share/{id}")
    @CheckLogin
    @Operation(summary = "Update resume share")
    public Result<ShareVO> update(@PathVariable Long id, @RequestBody ShareUpdateRequest request) {
        return Result.ok(shareService.updateShare(requireUserId(), id, request));
    }

    @DeleteMapping("/share/{id}")
    @CheckLogin
    @Operation(summary = "Close resume share")
    public Result<Boolean> close(@PathVariable Long id) {
        return Result.ok(shareService.closeShare(requireUserId(), id));
    }

    @GetMapping("/share/{id}/stats")
    @CheckLogin
    @Operation(summary = "Share access stats")
    public Result<ShareAccessStatsVO> stats(@PathVariable Long id) {
        return Result.ok(shareService.accessStats(requireUserId(), id));
    }

    @PostMapping("/public/share/{shortCode}")
    @Operation(summary = "Open shared resume")
    public Result<PublicResumeVO> publicResume(@PathVariable String shortCode,
                                               @RequestBody(required = false) ShareAccessRequest request) {
        return Result.ok(shareService.accessPublicResume(shortCode, request));
    }

    @GetMapping("/r/{shortCode}")
    @Operation(summary = "Redirect short link")
    public void redirect(@PathVariable String shortCode, HttpServletResponse response) throws IOException {
        response.sendRedirect(shareService.redirectTarget(shortCode));
    }

    private Long requireUserId() {
        Long userId = UserContext.currentUserId();
        if (userId == null) {
            throw new InfoException(InfoEnum.NOT_LOGIN);
        }
        return userId;
    }
}
