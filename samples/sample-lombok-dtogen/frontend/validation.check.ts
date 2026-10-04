/*
 * Checks that the generated schemas accept valid objects and reject invalid ones,
 * using the Standard Schema interface (https://standardschema.dev) implemented by Zod 4.
 * Run with: npm run check
 */
import type { StandardSchemaV1 } from "@standard-schema/spec";
import type { PersonDTO } from "./models.generated";
import { PersonDTOSchema } from "./validation.generated";

const validPerson: PersonDTO = {
    name: "Maria",
    birthDate: "1990-05-20",
    countryId: 1,
    status: "ACTIVE",
    phones: [{ type: "mobile", number: "+5563999999999" }],
    loginAttempts: 0,
};

const invalidPerson = {
    ...validPerson,
    name: "   ",
    email: "maria",
    birthDate: "2999-01-01",
    phones: [{ number: "123" }],
    loginAttempts: -1,
};

const expectedInvalidPaths = ["name", "email", "birthDate", "phones.0.number", "loginAttempts"];

function issuesOf(schema: StandardSchemaV1, value: unknown): readonly StandardSchemaV1.Issue[] {
    const result = schema["~standard"].validate(value);
    if (result instanceof Promise)
        throw new Error("The generated schemas must validate synchronously");

    return result.issues ?? [];
}

function pathOf(issue: StandardSchemaV1.Issue): string {
    return (issue.path ?? []).map(segment => typeof segment === "object" ? String(segment.key) : String(segment)).join(".");
}

const failures: string[] = [];
const validIssues = issuesOf(PersonDTOSchema, validPerson);
if (validIssues.length > 0)
    failures.push("valid person rejected: " + validIssues.map(issue => `${pathOf(issue)}: ${issue.message}`).join("; "));

const invalidIssues = issuesOf(PersonDTOSchema, invalidPerson);
const invalidPaths = invalidIssues.map(pathOf).sort();
if (JSON.stringify(invalidPaths) !== JSON.stringify([...expectedInvalidPaths].sort()))
    failures.push(`invalid person issues at [${invalidPaths}], expected [${expectedInvalidPaths}]`);

const portuguese = invalidIssues.some(issue => issue.message.includes("inválid"));
if (!portuguese)
    failures.push("messages are not in Portuguese (pt-BR locale): " + invalidIssues.map(issue => issue.message).join("; "));

invalidIssues.forEach(issue => console.log(`${pathOf(issue)}: ${issue.message}`));
if (failures.length > 0) {
    console.error(failures.join("\n"));
    process.exit(1);
}

console.log("Generated schemas are valid");
