package uk.codersparks.communitytreematerializedpath.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import uk.codersparks.communitytreematerializedpath.model.Community;
import uk.codersparks.communitytreematerializedpath.model.CommunityTreeNode;
import uk.codersparks.communitytreematerializedpath.service.CommunityService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/community")
public class CommunityController {

    private static final Logger logger = LoggerFactory.getLogger(CommunityController.class);

    private final CommunityService communityService;

    public CommunityController(CommunityService communityService) {
        this.communityService = communityService;
    }

    @GetMapping(path = "/")
    public ResponseEntity<List<String>> getCommunities() {
        return ResponseEntity.ok(communityService.getAllCommunities().stream().map(Community::getName).collect(Collectors.toList()));
    }

    @GetMapping(path = "/tree")
    public ResponseEntity<List<CommunityTreeNode>> getTree() {

        return ResponseEntity.ok(this.communityService.generateTree());
    }

    @PostMapping(path="/tree/{id}/parent/")
    public ResponseEntity<Void> moveCommunity(
            @PathVariable(name="id") String id
    ) {

        logger.info("Moving community: {} to root", id);

        this.communityService.moveCommunity(id, "");

        return ResponseEntity.noContent().build();
    }

    @PostMapping(path="/tree/{id}/parent/{parent}")
    public ResponseEntity<Void> moveCommunity(
            @PathVariable(name="id") String id,
            @PathVariable(name="parent") String parent
    ) {

        logger.info("Moving community: {} to parent: {}", id, parent);

        this.communityService.moveCommunity(id, parent);

        return ResponseEntity.noContent().build();
    }
}
