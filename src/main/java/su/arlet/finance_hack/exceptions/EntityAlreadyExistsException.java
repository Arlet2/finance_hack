package su.arlet.finance_hack.exceptions;

public class EntityAlreadyExistsException extends RuntimeException {
    public EntityAlreadyExistsException(String entityName) {
        super(entityName + " is already exists");
    }
}
