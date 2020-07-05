import _ from "underscore";
import React from "react";

export const splitToParagraphs = (text: string) =>
  _.map(text.split("\n"), (para, idx) => <p key={idx}>{para}</p>);
