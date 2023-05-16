package Model.Cache;

public interface CacheReplacementPolicy{
	void add(String word);
	String remove(); 
}
