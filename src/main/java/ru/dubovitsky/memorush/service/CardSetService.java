package ru.dubovitsky.memorush.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.dubovitsky.memorush.dto.request.CardSetRequestDto;
import ru.dubovitsky.memorush.facade.CardFacade;
import ru.dubovitsky.memorush.facade.CardSetFacade;
import ru.dubovitsky.memorush.facade.CategoryFacade;
import ru.dubovitsky.memorush.facade.TagFacade;
import ru.dubovitsky.memorush.model.CardSet;
import ru.dubovitsky.memorush.model.Category;
import ru.dubovitsky.memorush.model.Tag;
import ru.dubovitsky.memorush.model.User;
import ru.dubovitsky.memorush.repository.CardSetRepository;

import java.security.Principal;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
@Transactional
public class CardSetService {

    private CardSetRepository cardSetRepository;
    private final CategoryService categoryService;
    private final TagService tagService;
    private final UserService userService;

    public Optional<Collection<CardSet>> getAllCardSets() {
        return Optional.of(cardSetRepository.findAll());
    }

    public List<CardSet> searchAllCardSetsBySearchString(String search) {
        return cardSetRepository.findByCardSetNameOrCategoryName(search, search).orElseThrow(
                () -> new RuntimeException("CardSet not found with query: " + search));
    }

    public CardSet createCardSet(
            CardSetRequestDto cardSetRequestDto,
            Principal principal
    ) {
        //! Get data for creating entities from request
        User user = userService.getCurrentUserByPrincipal(principal);
        CardSet preSavedCardSet = CardSetFacade.cardSetRequestDtoToCardSet(cardSetRequestDto);

        //TODO Как то улучшить это! Получается мы тут должны получать уже сущность, готовую для сохранения!
        Category savedCategory = categoryService.createCategory(preSavedCardSet.getCategory());
        //TODO А тут мы получаем тэги из сервиса
        Set<Tag> preSavedTagsList = tagService.createTagCollection(preSavedCardSet.getTags());

        //! Set data (user, category, tagList) into CardSet entity
        preSavedCardSet.setUser(user);
        preSavedCardSet.setCategory(savedCategory);
        preSavedCardSet.setTags(preSavedTagsList);
        CardSet savedCardSet = cardSetRepository.save(preSavedCardSet);
        log.info(String.format("CardSet with id %s saved", savedCardSet.getId()));
        return savedCardSet;
    }

    public Optional<Set<CardSet>> getAllUserCardSetsList(Principal principal) {
        User user = userService.getCurrentUserByPrincipal(principal);
        return cardSetRepository.findAllByUser(user);
    }

    public boolean deleteCardSetById(Long id) {
        boolean present = cardSetRepository.findById(id).isPresent();
        if(!present) {
            log.info(String.format("CardSet with id %s not found!", id));
            return false;
        }
        cardSetRepository.deleteById(id);
        log.info(String.format("CardSet with id %s deleted", id));
        return true;
    }

    public CardSet updateCardSetById(Long id, CardSetRequestDto cardSetRequestDto) {
        CardSet cardSet = cardSetRepository.findById(id).orElseThrow(
                () -> new RuntimeException("CardSet not exist with id: " + id));

        //! Create or get new category
        Category newCategory = categoryService.createCategory(
                CategoryFacade.categoryNameToCategory(
                        cardSetRequestDto.getCategoryName())
        );
        //! Update category
        cardSet.setCategory(newCategory);

        //! Create or get new tag list
        Set<Tag> newTagsList = tagService.createTagCollection(
                TagFacade.tagRequestDtoToTagSet(
                        cardSetRequestDto.getTags()
                )
        );
        //! Update tagsList
        cardSet.setTags(newTagsList);

        cardSet.setName(cardSetRequestDto.getName());
        cardSet.setFavorite(cardSetRequestDto.isFavorite());
        cardSet.setDescription(cardSetRequestDto.getDescription());
        cardSet.setCardList(cardSetRequestDto.getFlashCardArray()
                .stream()
                .map(cardDto -> CardFacade.cardRequestDtoToCard(cardDto))
                .collect(Collectors.toSet()));

        CardSet updatedCardSet = cardSetRepository.save(cardSet);
        log.info(String.format("Card set with id: %s updated", id));
        return updatedCardSet;
    }
}
