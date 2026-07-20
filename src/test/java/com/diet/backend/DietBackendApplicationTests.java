package com.diet.backend;

import com.diet.backend.repository.MessageRepository;
import com.diet.backend.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class DietBackendApplicationTests {

	@Autowired
	private MessageRepository messageRepository;

	@Autowired
	private UserRepository userRepository;

	@Test
	void contextLoads() {
		System.out.println("=== USERS IN DATABASE ===");
		userRepository.findAll().forEach(user -> {
			System.out.println("ID: " + user.getId() + ", Name: " + user.getName() + ", Role: " + user.getRole() + ", Dietitian: " + (user.getDietitian() != null ? user.getDietitian().getName() : "None"));
		});

		System.out.println("=== MESSAGES IN DATABASE ===");
		messageRepository.findAll().forEach(msg -> {
			System.out.println("ID: " + msg.getId() + ", Sender: " + msg.getSender().getName() + ", Recipient: " + (msg.getRecipient() != null ? msg.getRecipient().getName() : "None") + ", Content: " + msg.getContent() + ", SentAt: " + msg.getSentAt());
		});
	}

}
