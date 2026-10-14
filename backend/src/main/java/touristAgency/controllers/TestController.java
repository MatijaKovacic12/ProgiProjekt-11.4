package touristAgency.controllers;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import touristAgency.dto.HelloWorldResponse;

@RestController
@RequestMapping(value = "/api/test",produces= MediaType.APPLICATION_JSON_VALUE)
public class TestController {
    int count =0;
    @GetMapping("/hello")
    public ResponseEntity<HelloWorldResponse> HelloWorld(){
        count++;
        return ResponseEntity.ok(new HelloWorldResponse("Hello world po "+count+". put !!!"));
    }
}
