package calculator;

public class CalculatorException extends RuntimeException {

    private String description;
    
    public CalculatorException(String message) {
        super(message);
    }

    public CalculatorException(String message, String description) {
        super(message);
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

}
