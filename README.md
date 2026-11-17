# Assembler

A desktop assembler that converts assembly code into hexadecimal machine code for a custom 16-bit processor. It was built with teammates for a computer architecture course project, to generate the program images loaded into our processor.

## Features

- Assembles a 34-instruction set across four formats (R, I, B, J) into 16-bit words, shown as 4 hex digits per line
- Two-pass label resolution, so branches and jumps can target labels defined later in the file
- `#` comments, blank lines, and case-insensitive mnemonics and register names
- Swing GUI with **Assemble**, **Clear**, **Copy** and **Split** (16-bit words to 8-bit bytes)
- Prints each instruction's 16-bit binary to the console, plus explanations for errors

## Quick start

**Requirements:** Java 17 or later and a desktop environment (the GUI uses Swing).

### Run the prebuilt jar

```bash
java -jar Assembler.jar
```

### Build from source

```bash
cd Assembler
mkdir -p build
javac -d build src/*.java
java -cp build AssemblerGUI
```

To package your own jar: `jar cfe Assembler.jar AssemblerGUI -C build .`

You can also open `Assembler/` in IntelliJ IDEA and run `AssemblerGUI`. `Main` is a retired entry point (see [Repository layout](#repository-layout)); don't run it.

## Using the GUI

Type or paste assembly into the left pane and click **Assemble**. The hex output appears in the right pane, one instruction per line.

| Button | Action |
|---|---|
| **Assemble** | Assembles the left pane and fills the right pane. Enables Copy and Split. |
| **Clear** | Empties both panes. |
| **Copy** | Copies the output to the clipboard. The right pane is then replaced by a thank-you message, so press Assemble again if you need the output back. |
| **Split** | Splits each 16-bit word into two bytes (high byte first), one per line, for byte-wide memories. Use it only on error-free output. |

## Assembly language reference

The processor has 8 registers, `r0`–`r7`, and every instruction is 16 bits wide.

### Instruction formats

| Format | Layout (bits) | Operand syntax |
|---|---|---|
| **R** | opcode(5) · reg(3) · reg(3) · reg(3) · func(2) | `ADD r1,r2,r3` |
| **I** | opcode(5) · reg(3) · reg(3) · imm(5) | `ADDI r1,r2,5` · `LW r0,r6,10` · `BEQ r0,r2,LABEL` |
| **B** | opcode(5) · reg(3) · imm/offset(8) | `BEQZ r5,LABEL` · `JR r5,60` · `SET r6,13` |
| **J** | opcode(5) · offset(11) | `J LABEL` |

Register fields are encoded in the order written.

### Instruction set

| Format | Opcode | Instructions |
|---|---|---|
| R | 0 (`00000`) | `AND` `OR` `XOR` `EQV` (func `00`, `01`, `10`, `11`) |
| R | 1 (`00001`) | `ADD` `SUB` `SLT` `SEQ` (func `00`, `01`, `10`, `11`) |
| I | 4–17 | `ANDI` `ORI` `XORI` `EQVI` `ADDI` `SLTI` `SEQI` `SLL` `SRL` `ROR` `BEQ` `BNE` `LW` `SW` (consecutive opcodes, in this order) |
| B | 20–29 | `BEQZ` `BNEZ` `BLTZ` `BGEZ` `BGTZ` `BLEZ` `JR` `JALR` `SET` `SSET` (consecutive opcodes, in this order) |
| J | 30–31 | `J` `JAL` |

### Source rules

- One instruction per line; separate operands with commas and **no spaces** (`ADD r1,r2,r3`).
- A label is a line of its own: `NAME:`. Label names are case-sensitive; mnemonics and registers are not.
- A comment is a line containing `#`. The whole line is ignored, so put comments on their own line.
- Immediates are decimal. Negative values are stored in two's complement.

### How addresses and offsets are encoded

Addresses are 0-based instruction indices. Labels, comments and blank lines do not take an address.

| Instruction | Field | Value |
|---|---|---|
| `BEQ`, `BNE` | 5-bit offset | label − address − 1 |
| `BEQZ`, `BNEZ`, `BLTZ`, `BGEZ`, `BGTZ`, `BLEZ` | 8-bit offset | label − address − 1 |
| `J`, `JAL` | 11-bit offset | label − address |

## Example

Source from [`Assembler/Code.txt`](Assembler/Code.txt) and the output of the current build:

| Addr | Source | Hex |
|---|---|---|
| 0 | `AND r1,r6,r5` | `01d4` |
| 1 | `OR r0,r5,r1` | `00a5` |
| 2 | `Add r0,r5,r1` | `08a4` |
| 3 | `XOR r5,r6,r6` | `05da` |
| | `WHILE:` | label = 4 |
| 4 | `ROR r0,r6,10` | `68ca` |
| 5 | `ORI r6,r6,20` | `2ed4` |
| | `#this is a comment to test comments` | ignored |
| 6 | `SW r0,r0,15` | `880f` |
| | `ANOTHER:` | label = 7 |
| 7 | `LW r0,r6,10` | `80ca` |
| 8 | `BEQ r0,r2,WHILE` | `705b` |
| 9 | `BEQZ r5,WHILE` | `a5fa` |
| 10 | `JR r5,60` | `d53c` |
| 11 | `SET r6,13` | `e60d` |
| 12 | `J WHILE` | `f7f8` |
| 13 | `JAL ANOTHER` | `fffa` |

## Error handling

Problems are reported in the output pane as `Error in line N`, followed by a short technical message. **`N` is the instruction's 0-based address, not the line number in the editor.** More detail (the offending instruction and why) is printed to the console. Typical causes are an unknown mnemonic, a register other than `r0`–`r7`, an undefined label, an out-of-range immediate, or spaces after commas.

## Known limitations

- **Silent drops:** a line containing `#` is skipped entirely, including any instruction before the `#`. A line containing `:` is treated as a label, so `LOOP: ADD r1,r2,r3` defines a bogus label and drops the instruction.
- **Wrapping values:** immediates and label offsets are truncated to their field width without error. The immediate range checks (-32..31 for I-type, -256..255 for B-type) are wider than what the fields can hold, and label offsets are not range-checked.
- **Stale labels:** the label table is static and is never cleared, so within one GUI session a label from an earlier run still resolves even after you delete it from the source. Restart the app to reset it.
- **Error reporting:** error lines show raw exception text and use instruction addresses rather than editor line numbers.

## Repository layout

```
Assembler.jar                           Prebuilt GUI (matches the current source)
Assembler/src/Assembler.java            Instruction encoder: opcodes, registers, labels, hex output
Assembler/src/AssemblerGUI.java         Swing front end and the two-pass assembly loop
Assembler/src/Main.java                 Retired command-line version (entirely commented out)
Assembler/Code.txt, OUTPUT.txt          Sample program and sample output from the earlier command-line version
Assembler/out/                          IDE build output and an older jar
```
