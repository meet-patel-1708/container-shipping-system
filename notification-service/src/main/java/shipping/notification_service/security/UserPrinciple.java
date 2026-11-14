package shipping.notification_service.security;



public record UserPrinciple(Long userId, String email, String role) {}
