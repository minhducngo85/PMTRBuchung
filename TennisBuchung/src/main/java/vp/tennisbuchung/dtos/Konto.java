package vp.tennisbuchung.dtos;

public record Konto(String lastname, String password,
	// For the future booking
	int preparationBeforeMinute) {
}
