package com.elearning.service.services;

import com.elearning.service.dtos.CreateDeckDTO;
import com.elearning.service.dtos.DeckDTO;
import com.elearning.service.entities.Deck;
import com.elearning.service.entities.User;
import com.elearning.service.repositories.DeckRepository;
import com.elearning.service.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.access.AccessDeniedException;
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

    public List<DeckDTO> getDecksForCurrentUser() {
        // Yêu cầu Copilot: Viết logic hoàn chỉnh cho phương thức getDecksForCurrentUser.
        // Bối cảnh: Lấy tất cả các bộ thẻ thuộc về người dùng đang đăng nhập.
        //
        // Luồng xử lý chi tiết như sau:
        // 1. Lấy thông tin người dùng đang đăng nhập từ SecurityContextHolder (tương tự như trên).
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        
        // 2. Tìm đối tượng User trong CSDL bằng email. Nếu không thấy, ném ngoại lệ.
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        
        // 3. Truy vấn danh sách bộ thẻ:
        //    - Gọi phương thức deckRepository.findAllByUserId(user.getId()) để lấy về một List<Deck>.
        List<Deck> decks = deckRepository.findAllByUserId(user.getId());
        
        // 4. Chuyển đổi List<Deck> thành List<DeckDTO>:
        //    - Sử dụng Java Stream API (.stream()) kết hợp với ModelMapper.
        //    - Với mỗi Deck trong danh sách, ánh xạ nó thành một DeckDTO.
        //    - Trong quá trình ánh xạ, tính toán và gán giá trị cho trường 'cardCount' của DeckDTO bằng cách lấy deck.getCards().size().
        //    - Thu thập kết quả thành một List<DeckDTO>.
        return decks.stream()
                .map(deck -> {
                    DeckDTO deckDTO = modelMapper.map(deck, DeckDTO.class);
                    deckDTO.setCardCount(deck.getCards() != null ? deck.getCards().size() : 0);
                    return deckDTO;
                })
                .collect(Collectors.toList());
        
        // 5. Trả về danh sách DTO.
    }
    
    public DeckDTO getDeckById(Long deckId) {
        Deck deck = getAndVerifyDeckOwnership(deckId);
        DeckDTO deckDTO = modelMapper.map(deck, DeckDTO.class);
        deckDTO.setCardCount(deck.getCards() != null ? deck.getCards().size() : 0);
        return deckDTO;
    }

    public List<DeckDTO> getDecksByCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
        
        List<Deck> decks = deckRepository.findAllByUserId(user.getId());
        
        return decks.stream()
                .map(deck -> {
                    DeckDTO deckDTO = modelMapper.map(deck, DeckDTO.class);
                    deckDTO.setCardCount(deck.getCards() != null ? deck.getCards().size() : 0);
                    return deckDTO;
                })
                .collect(Collectors.toList());
    }

    public DeckDTO createDeck(CreateDeckDTO request) {
        // Yêu cầu Copilot: Viết logic hoàn chỉnh cho phương thức createDeck.
        // Bối cảnh: Phương thức này được gọi bởi DeckController khi người dùng muốn tạo một bộ thẻ mới.
        //
        // Luồng xử lý chi tiết như sau:
        // 1. Lấy thông tin người dùng đang đăng nhập:
        //    - Sử dụng SecurityContextHolder để lấy Authentication object.
        //    - Từ đó, lấy ra email của người dùng (chính là username).
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        
        // 2. Tìm đối tượng User trong CSDL:
        //    - Dùng userRepository.findByEmail(email) để tìm.
        //    - Nếu không tìm thấy, ném ra một ngoại lệ UsernameNotFoundException với thông báo "User not found".
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        
        // 3. Tạo và ánh xạ dữ liệu:
        //    - Khởi tạo một đối tượng Deck mới.
        //    - Sử dụng ModelMapper để ánh xạ các thuộc tính từ 'request' (CreateDeckDTO) sang đối tượng Deck vừa tạo.
        Deck deck = new Deck();
        modelMapper.map(request, deck);
        
        // 4. Thiết lập mối quan hệ:
        //    - Gán đối tượng User vừa tìm được vào cho bộ thẻ mới (deck.setUser(user)).
        deck.setUser(user);
        
        // 5. Lưu vào CSDL:
        //    - Gọi deckRepository.save(deck) và hứng kết quả vào một biến savedDeck.
        Deck savedDeck = deckRepository.save(deck);
        
        // 6. Trả về kết quả:
        //    - Dùng ModelMapper để ánh xạ 'savedDeck' (kiểu Deck) thành 'DeckDTO'.
        //    - Thêm số lượng thẻ (cardCount) cho DeckDTO (hiện tại là 0).
        //    - Return DeckDTO.
        DeckDTO deckDTO = modelMapper.map(savedDeck, DeckDTO.class);
        deckDTO.setCardCount(0); // Bộ thẻ mới tạo có 0 thẻ
        
        return deckDTO;
    }

    private Deck getAndVerifyDeckOwnership(Long deckId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        
        Deck deck = deckRepository.findById(deckId)
                .orElseThrow(() -> new RuntimeException("Deck not found with id: " + deckId));
        
        if (!deck.getUser().getEmail().equals(email)) {
            throw new AccessDeniedException("Bạn không có quyền truy cập bộ thẻ này");
        }
        
        return deck;
    }

    public DeckDTO updateDeck(Long deckId, CreateDeckDTO deckDetails) {
        Deck deck = getAndVerifyDeckOwnership(deckId);
        
        deck.setName(deckDetails.getName());
        deck.setDescription(deckDetails.getDescription());
        
        Deck updatedDeck = deckRepository.save(deck);
        
        DeckDTO deckDTO = modelMapper.map(updatedDeck, DeckDTO.class);
        deckDTO.setCardCount(updatedDeck.getCards() != null ? updatedDeck.getCards().size() : 0);
        return deckDTO;
    }

    public void deleteDeck(Long deckId) {
        getAndVerifyDeckOwnership(deckId);
        deckRepository.deleteById(deckId);
    }
}
