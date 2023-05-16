package Model.cache;

public interface CacheReplacementPolicy{
	void add(String word);
	String remove(); 
}
