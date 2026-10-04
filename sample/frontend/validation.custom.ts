/*
 * Schemas written by hand for the classes excluded from validation
 * (check the <validation><excludeClasses> in the sample pom.xml).
 * They're imported by the generated validation.generated.ts file.
 */
import { z } from "zod";
import type { Phone } from "./models.generated";

/** Accepts only phone numbers with 8 to 15 digits, optionally starting with +. */
export const PhoneSchema: z.ZodType<Phone> = z.object({
    type: z.string().nullish(),
    number: z.string().regex(/^\+?\d{8,15}$/).nullish(),
});
