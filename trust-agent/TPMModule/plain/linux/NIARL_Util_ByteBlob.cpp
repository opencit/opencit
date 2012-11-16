/**
 * Copyright (C) 2012, U.S. Government, National Security Agency, National Information Assurance Research Laboratory
 * This is a work of the UNITED STATES GOVERNMENT and is not subject to copyright protection in the United States. Foreign copyrights may apply.
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * 1) Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * 2) Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 * 3) Neither the name of the NATIONAL SECURITY AGENCY/NATIONAL INFORMATION ASSURANCE RESEARCH LABORATORY nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

#include "NIARL_Util_ByteBlob.h"

NIARL_Util_ByteBlob::NIARL_Util_ByteBlob(string in_var)
{
	short	temp_size = in_var.size();	//get the full string length
	if(temp_size % 2 == 1)
		throw NIARL_TPM_ModuleV2::ERROR_ARG_VALIDATION;

	size = temp_size / 2;	//base 16 takes 2 digits so reduce length to show real size
	blob = new BYTE[size];	//create the byte array

	UINT32	hex_value = 0;	//accumulates 2 hex characters to load into blob
	short	index;			//index that points at correct byte blob index (truncation intentional)

	for(short i = 0; i < temp_size; i++)
	{
		switch(in_var[i])
		{
		case 'F':
		case 'f':
			hex_value += 15;
			break;
		case 'E':
		case 'e':
			hex_value += 14;
			break;
		case 'D':
		case 'd':
			hex_value += 13;
			break;
		case 'C':
		case 'c':
			hex_value += 12;
			break;
		case 'B':
		case 'b':
			hex_value += 11;
			break;
		case 'A':
		case 'a':
			hex_value += 10;
			break;
		case '9':
			hex_value += 9;
			break;
		case '8':
			hex_value += 8;
			break;
		case '7':
			hex_value += 7;
			break;
		case '6':
			hex_value += 6;
			break;
		case '5':
			hex_value += 5;
			break;
		case '4':
			hex_value += 4;
			break;
		case '3':
			hex_value += 3;
			break;
		case '2':
			hex_value += 2;
			break;
		case '1':
			hex_value += 1;
			break;
		case '0':
			hex_value += 0;
			break;
		default:
			throw NIARL_TPM_ModuleV2::ERROR_ARG_VALIDATION;
		}

		index = i / 2;		//allow truncation so we get the right spot in the byte array
		if(i % 2 == 0)		//even characters are the first of two hex characters
			hex_value *= 16;
		else
		{
			blob[index] = hex_value;	//we now have 2 hex characters so load them into the byte array
			hex_value = 0;				//reset the accumulator
		}
	}
}

NIARL_Util_ByteBlob::~NIARL_Util_ByteBlob()
{
	delete [] blob;
}

void NIARL_Util_ByteBlob::Print()
{
	for(UINT32 i = 0; i < size; i++)
		cout << setbase(16) << setw(2) << setfill('0') << (int)blob[i];
}
