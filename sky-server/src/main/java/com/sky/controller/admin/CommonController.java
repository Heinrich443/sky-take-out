package com.sky.controller.admin;

import com.sky.constant.MessageConstant;
import com.sky.result.Result;
import com.sky.utils.AliOssUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.UUID;

/**
 * 通用接口
 */
@Api(tags = "通用接口")
@Slf4j
@RestController
@RequestMapping("/admin/common")
public class CommonController {

    @Resource
    private AliOssUtil aliOssUtil;

    /**
     * 文件上传
     * @param file
     * @return
     */
    @ApiOperation("文件上传")
    @PostMapping("/upload")
    public Result<String> upload(@RequestParam MultipartFile file) {
        log.info("文件上传：{}", file);

        try {
            // 获取原始文件名
            String originalFilename = file.getOriginalFilename();
            // 截取文件扩展名
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            // 拼接新的文件名，使用UUID
            String objName = UUID.randomUUID() + extension;
            // 返回文件访问路径
            String path = aliOssUtil.upload(file.getBytes(), objName);
            return Result.success(path);
        } catch (IOException e) {
            log.error("文件上传失败：{}", e);
        }

        return Result.error(MessageConstant.UPLOAD_FAILED);
    }
}
