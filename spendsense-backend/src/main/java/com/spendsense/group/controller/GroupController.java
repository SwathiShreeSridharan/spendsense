package com.spendsense.group.controller;

import com.spendsense.group.dto.CreateGroupRequest;
import com.spendsense.group.dto.GroupResponse;
import com.spendsense.group.dto.UpdateGroupRequest;
import com.spendsense.group.service.GroupService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/groups")
public class GroupController {

    private final GroupService groupService;

    public GroupController(GroupService groupService) {
        this.groupService = groupService;
    }

    @PostMapping
    public ResponseEntity<GroupResponse> createGroup(
            @Valid @RequestBody CreateGroupRequest request)
    {
        GroupResponse response =
                groupService.createGroup(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<GroupResponse>> getMyGroups() {

        List<GroupResponse> response =
                groupService.getMyGroups();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{groupId}")
    public ResponseEntity<GroupResponse> getGroupById(
            @PathVariable UUID groupId)
    {
        GroupResponse response =
                groupService.getGroupById(groupId);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{groupId}")
    public ResponseEntity<GroupResponse> updateGroup(
            @PathVariable UUID groupId,
            @Valid @RequestBody UpdateGroupRequest request)
    {
        GroupResponse response =
                groupService.updateGroup(
                        groupId,
                        request
                );

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{groupId}")
    public ResponseEntity<Void> archiveGroup(
            @PathVariable UUID groupId
    ) {

        groupService.archiveGroup(groupId);

        return ResponseEntity.noContent().build();
    }
}
