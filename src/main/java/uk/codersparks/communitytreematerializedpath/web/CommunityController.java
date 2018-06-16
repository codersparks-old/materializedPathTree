package uk.codersparks.communitytreematerializedpath.web;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.codersparks.communitytreematerializedpath.model.CommunityTreeNode;
import uk.codersparks.communitytreematerializedpath.service.CommunityService;

@RestController
@RequestMapping("/api/community")
public class CommunityController {

    private final CommunityService communityService;

    public CommunityController(CommunityService communityService) {
        this.communityService = communityService;
    }

    @GetMapping(path = "/tree")
    public ResponseEntity<CommunityTreeNode> getTree() {

        return ResponseEntity.ok(this.communityService.generateTree());
    }
}
