package ps.enumerators;

/**
 * Enumerates all association types.
 */
public enum AssociationCaseEnum {

	ONE(1), 
	TWO(2), 
	THREE(3);
	
	private int value;

	private AssociationCaseEnum(int value) {
		this.value = value;
	}
	
	public int getValue(){
		return this.value;
	}
	
}
