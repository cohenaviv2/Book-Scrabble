package model.server.cache;

public interface CacheReplacementPolicy{
	void add(String word);
	String remove(); 
}
