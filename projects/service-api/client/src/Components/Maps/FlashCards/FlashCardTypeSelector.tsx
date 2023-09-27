import React from 'react';
import { Button, ModalHeader, ModalBody, ModalFooter, Media } from 'reactstrap';
import { FlashcardTypeBrief } from 'src/api/models';
import { RootState } from 'src/redux';
import { setFlashcardTypes } from 'src/redux/modules/mindmap';
import { connect } from 'react-redux';
import { LoadingSpinner } from 'src/Components/Animations/LoadingSpinner';

export const getFlashCardIconByCardTypeName = {
  standard: 'fcType_basic.png',
  code_exercise_scala: 'fcType_code_exercise_scala.png',
  code_exercise_nodejs: 'fcType_code_exercise_nodejs.png',
};

const mapStateToProps = (state: RootState) => ({
  flashcardTypes: state.mindmap.flashcardTypes,
});

const mapDispatchToProps = { setFlashcardTypes };

export interface FlashCardProps {
  // tslint:disable-next-line: no-any
  toggleFunction: () => void;
  liftCardType: (cardType: FlashcardTypeBrief) => void;
}

type Props = ReturnType<typeof mapStateToProps> &
  typeof mapDispatchToProps &
  FlashCardProps;

const UnconnectedFlashCardTypeSelector: React.FunctionComponent<Props> = (
  props,
) => {
  function selectCardType(cardType: FlashcardTypeBrief) {
    props.liftCardType(cardType);
  }

  function getCardTypeLogo(name: string) {
    return '/assets/images/icons/' + getFlashCardIconByCardTypeName[name];
  }

  if (!props.flashcardTypes) {
    window.mpio.getFlashcardTypes((types) => {
      props.setFlashcardTypes(types);
    });
    return <LoadingSpinner />;
  } else {
    return (
      <>
        <ModalHeader toggle={props.toggleFunction}>
          Select Flashcard Type
        </ModalHeader>
        <ModalBody role="document">
          {props.flashcardTypes.map((v, i) => {
            return (
              <Media
                key={v.id}
                as="li"
                className="p-2 pointer"
                onClick={() => selectCardType(v)}
              >
                <img
                  width={64}
                  height={64}
                  className="mr-3"
                  src={getCardTypeLogo(v.name)}
                />
                <Media body={true}>
                  <h5>{v.commonName}</h5>
                  <p>{v.description}</p>
                </Media>
              </Media>
            );
          })}
        </ModalBody>
        <ModalFooter>
          <Button color="danger" onClick={props.toggleFunction}>
            Cancel
          </Button>
        </ModalFooter>
      </>
    );
  }
};

const FlashCardTypeSelector = connect(
  mapStateToProps,
  mapDispatchToProps,
)(UnconnectedFlashCardTypeSelector);

export default FlashCardTypeSelector;
