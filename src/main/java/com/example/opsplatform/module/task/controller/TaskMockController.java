package com.example.opsplatform.module.task.controller;

import com.example.opsplatform.common.core.domain.Result;
import java.util.Map;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 任务模拟接口。
 *
 * 这个接口不是正式任务模块，只用于第一阶段验证 RBAC：
 * - admin 拥有 task:execute，可以调用成功。
 * - viewer 没有 task:execute，登录后调用应返回 403。
 *
 * 等后续进入巡检任务模块时，会用真实 ops_task / ops_execution_record 表替换这里的 mock 返回。
 */
@RestController
@RequestMapping("/api/v1/tasks")
public class TaskMockController {

    /**
     * 模拟执行任务。
     *
     * @PreAuthorize 是接口级授权点。它不负责识别当前用户是谁，
     * 只负责判断当前用户 authorities 里有没有 task:execute。
     */
    @PreAuthorize("hasAuthority('task:execute')")
    @PostMapping("/{id}/mock-execute")
    public Result<Map<String, Object>> mockExecute(@PathVariable Long id) {
        return Result.success(Map.of(
                "taskId", id,
                "status", "MOCK_EXECUTED"
        ));
    }
}
