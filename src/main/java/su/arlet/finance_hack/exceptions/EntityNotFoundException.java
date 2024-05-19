package su.arlet.finance_hack.exceptions;

public class EntityNotFoundException extends RuntimeException {
    public EntityNotFoundException(String entityName) {
        super(entityName + " is not found");
    }
}
