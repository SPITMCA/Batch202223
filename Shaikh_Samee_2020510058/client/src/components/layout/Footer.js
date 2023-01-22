import React from "react";

export default function Footer() {
  return (
    <footer className="bg-dark text-white mt-5 p-2 text-center">
      Copyright &copy; {new Date().getFullYear()} Socio
    </footer>
  );
}
