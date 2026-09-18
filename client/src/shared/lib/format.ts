import DecimalJs from "decimal.js";
import type { Decimal } from "../api/contracts";

const parts = new Intl.NumberFormat(undefined).formatToParts(1000.1);
const groupSeparator = parts.find((part) => part.type === "group")?.value ?? ",";
const decimalSeparator = parts.find((part) => part.type === "decimal")?.value ?? ".";

export function asDecimal(value: Decimal): DecimalJs {
  try {
    return new DecimalJs(value);
  } catch {
    return new DecimalJs(0);
  }
}

export function isValidDecimal(value: string): boolean {
  if (!value.trim()) return false;
  try {
    return new DecimalJs(value).isFinite();
  } catch {
    return false;
  }
}

export function isPositiveDecimal(value: string): boolean {
  return isValidDecimal(value) && new DecimalJs(value).greaterThan(0);
}

/** Currency is not yet modelled by the API, so this intentionally shows a neutral amount. */
export function formatAmount(value: Decimal | DecimalJs): string {
  const decimal = value instanceof DecimalJs ? value : asDecimal(value);
  const [integer, fraction] = decimal.toFixed(2).split(".");
  const sign = integer.startsWith("-") ? "-" : "";
  const digits = sign ? integer.slice(1) : integer;
  const grouped = digits.replace(/\B(?=(\d{3})+(?!\d))/g, groupSeparator);
  return `${sign}${grouped}${decimalSeparator}${fraction}`;
}

export function formatDateTime(instant: string): string {
  const date = new Date(instant);
  if (Number.isNaN(date.getTime())) {
    return instant;
  }
  return new Intl.DateTimeFormat(undefined, {
    dateStyle: "medium",
    timeStyle: "short",
  }).format(date);
}

export function dateTimeInputToInstant(value: string): string {
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) {
    throw new Error("Choose a valid date and time.");
  }
  return date.toISOString();
}
