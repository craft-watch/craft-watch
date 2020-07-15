import React from "react";
import { toSafePathPart } from "../utils/stuff";
import Link from "next/link";

interface Props {
  shortName: string;
}

export const BreweryLink: React.FC<Props> = (props) => (
  <Link href="/[id]" as={`/${toSafePathPart(props.shortName)}`}>
    <a>{props.children}</a>
  </Link>
);
