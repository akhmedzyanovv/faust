package org.mpi.faust.web;


import org.mpi.faust.model.*;
import org.mpi.faust.security.CurrentUser;
import org.mpi.faust.security.UserPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.Principal;
import java.util.Collection;
import java.util.Optional;

import static org.springframework.http.ResponseEntity.ok;

@RestController
@RequestMapping("/api/v1/treasury")
public class TreasuryController {
    private IssueRepository issueRepository;
    private SupplyRepository supplyRepository;
    public TreasuryController(IssueRepository issueRepository, SupplyRepository supplyRepository) {
        this.issueRepository = issueRepository;
        this.supplyRepository = supplyRepository;
    }

    private static boolean checkUSerForRole(UserPrincipal principal, String role)
    {
        return principal.getAuthorities().stream().anyMatch((p) -> ((GrantedAuthority) p).getAuthority().equals(role));
    }

    @GetMapping("/issues")
    @PreAuthorize("hasRole({'EMPEROR', 'TREASURY'})")
    Collection<Issue> issues(Principal principal) {
        return issueRepository.findAll();
    }

    @GetMapping("/issues/{id}")
    @PreAuthorize("hasRole({'EMPEROR', 'TREASURY'})")
    ResponseEntity<?> getIssue(@PathVariable Long id, @CurrentUser UserPrincipal principal) {
        Optional<Issue> issue = issueRepository.findById(id);
        return issue.map(response -> ok().body(response))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @DeleteMapping("/issues/{id}")
    @PreAuthorize("hasRole({'EMPEROR', 'TREASURY'})")
    ResponseEntity<?> delIssue(@PathVariable Long id) {
        issueRepository.deleteById(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/issues")
    @PreAuthorize("hasRole({'EMPEROR', 'TREASURY'})")
    ResponseEntity<Issue> createIssue(@Valid @RequestBody Issue issue) throws URISyntaxException {
        Issue result = issueRepository.save(issue);
        return ResponseEntity.created(new URI("/api/v1/treasury/issues/" + result.getId()))
                .body(result);
    }

    @PutMapping("/issues")
    @PreAuthorize("hasRole({'EMPEROR', 'TREASURY'})")
    ResponseEntity<Issue> modifyIssue(@Valid @RequestBody Issue issue, @CurrentUser UserPrincipal principal) {
        if (issue.getState() != IssueState.New) {
            if (checkUSerForRole(principal, "Emperor"))
            {
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            }
        }
        Issue result = issueRepository.save(issue);
        return ok(result);
    }


    @GetMapping("/supplies")
    @PreAuthorize("hasRole({'TREASURY', 'SUPPLIER'})")
    Collection<Supply> GetAllSupplies(){
        return supplyRepository.findAll();
    }

    @GetMapping("/supplies/{id}")
    @PreAuthorize("hasRole({'TREASURY', 'SUPPLIER'})")
    ResponseEntity<?> GetSupply(@PathVariable Long id){
        Optional<Supply> issue = supplyRepository.findById(id);
        return issue.map(response -> ok().body(response))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @DeleteMapping("/supplies/{id}")
    @PreAuthorize("hasRole({'TREASURY', 'SUPPLIER'})")
    ResponseEntity DeleteSupply(@PathVariable Long id){
        supplyRepository.deleteById(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/supplies")
    @PreAuthorize("hasRole({'TREASURY', 'SUPPLIER'})")
    ResponseEntity<Supply> CreateSupply(@Valid @RequestBody Supply supply, @CurrentUser UserPrincipal principal) throws URISyntaxException {
        supply.setOwner(principal.getId());
        Supply result = supplyRepository.save(supply);
        return ResponseEntity.created(new URI("/api/v1/treasury/supplies/" + result.getId()))
                .body(result);
    }

    @PutMapping("/supplies/{id}")
    @PreAuthorize("hasRole({'TREASURY', 'SUPPLIER'})")
    ResponseEntity<?> UpdateSupply(@PathVariable Long id, @Valid @RequestBody Supply supply, @CurrentUser UserPrincipal principal) {
        Optional<Supply> supply1_opt = supplyRepository.findById(id);
        if (!supply1_opt.isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        Supply supply1 = supply1_opt.get();
        if(checkUSerForRole(principal, "Treasury")) {
            supply1.setStatus("Approved");
            supplyRepository.saveAndFlush(supply1);
            return ok(supply1);
        }
        else if(checkUSerForRole(principal, "Supplier")) {
            supply1.setItems(supply.getItems());
            supply1.setStatus(supply.getStatus());
            supplyRepository.saveAndFlush(supply1);
            return ok(supply1);
        }

        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
}
