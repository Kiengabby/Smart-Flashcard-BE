package com.elearning.service.services;

import com.elearning.service.dtos.CreateDeckDTO;
import com.elearning.service.dtos.DeckDTO;
import com.elearning.service.entities.Deck;
import com.elearning.service.entities.User;
import com.elearning.service.repositories.DeckRepository;
import com.elearning.service.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DeckService {

    private final DeckRepository deckRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    public List<DeckDTO> getDecksByCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
        
        List<Deck> decks = deckRepository.findAllByUserId(user.getId());
        
        return decks.stream()
                .map(deck -> modelMapper.map(deck, DeckDTO.class))
                .collect(Collectors.toList());
    }

    public DeckDTO createDeck(CreateDeckDTO createDeckDTO) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
        
        Deck deck = new Deck();
        deck.setName(createDeckDTO.getName());
        deck.setDescription(createDeckDTO.getDescription());
        deck.setUser(user);
        
        Deck savedDeck = deckRepository.save(deck);
        
        return modelMapper.map(savedDeck, DeckDTO.class);
    }
}
