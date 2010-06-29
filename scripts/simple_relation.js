use ( "Relations.use.js" );

function isReviewOf ( submitter, format, language, xml, pid ) {
  Relations.isReviewOf ( xml, pid );
}

function hasReview ( submitter, format, language, xml, pid ) {
  Relations.hasReview ( xml, pid );
}

function hasTrack ( submitter, format, language, xml, pid ) {
  Relations.hasTrack ( xml, pid );
}

function isPartOfAlbum ( submitter, format, language, xml, pid ) {
  Relations.isPartOfAlbum ( xml, pid );
}

function hasSoundClip ( submitter, format, language, xml, pid ) {
  Relations.hasSoundClip ( xml, pid );
}

function isAuthorDescriptionOf ( submitter, format, language, xml, pid ) {
  Relations.isAuthorDescriptionOf ( xml, pid );
}
