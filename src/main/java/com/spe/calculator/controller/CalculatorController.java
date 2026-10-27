package com.spe.calculator.controller;

import com.spe.calculator.model.CalculationResponse;
import com.spe.calculator.service.CalculatorService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class CalculatorController {

    private final CalculatorService calculatorService;

    public CalculatorController(CalculatorService calculatorService) {
        this.calculatorService = calculatorService;
    }

    @GetMapping("/add")
    public CalculationResponse add(@RequestParam double a, @RequestParam double b) {
        return respond("add", a, b, calculatorService.add(a, b));
    }

    @GetMapping("/subtract")
    public CalculationResponse subtract(@RequestParam double a, @RequestParam double b) {
        return respond("subtract", a, b, calculatorService.subtract(a, b));
    }

    @GetMapping("/multiply")
    public CalculationResponse multiply(@RequestParam double a, @RequestParam double b) {
        return respond("multiply", a, b, calculatorService.multiply(a, b));
    }

    @GetMapping("/divide")
    public CalculationResponse divide(@RequestParam double a, @RequestParam double b) {
        return respond("divide", a, b, calculatorService.divide(a, b));
    }

    // Spring parses "NaN" and "Infinity" as valid doubles, and large inputs can overflow,
    // so reject anything that is not a finite number.
    private CalculationResponse respond(String operation, double a, double b, double result) {
        if (!Double.isFinite(a) || !Double.isFinite(b)) {
            throw new IllegalArgumentException("Parameters 'a' and 'b' must be finite numbers");
        }
        if (!Double.isFinite(result)) {
            throw new IllegalArgumentException("Result is out of range");
        }
        return new CalculationResponse(operation, a, b, result);
    }
}
