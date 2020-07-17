import React from "react";
import Link from "next/link";

interface Props {
  id: string;
}

export const BreweryLink: React.FC<Props> = (props) => (
  <Link href="/[id]" as={`/${props.id}`}>
    <a>{props.children}</a>
  </Link>
);
