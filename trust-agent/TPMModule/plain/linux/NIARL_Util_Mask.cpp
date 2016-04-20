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

#include "NIARL_Util_Mask.h"

NIARL_Util_Mask::NIARL_Util_Mask(string in_var)
{
	short temp_size = in_var.size();
	vector<int> temp_array;

	int		bumper = 0;

	for(short i = 0; i < temp_size; i++)
	{
		switch(in_var[i])
		{
		case 'F':
		case 'f':
			temp_array.push_back(bumper + 0);
			temp_array.push_back(bumper + 1);
			temp_array.push_back(bumper + 2);
			temp_array.push_back(bumper + 3);
			//Bitmask 1111
			break;
		case 'E':
		case 'e':
			temp_array.push_back(bumper + 0);
			temp_array.push_back(bumper + 1);
			temp_array.push_back(bumper + 2);
			//temp_array.push_back(bumper + 3);
			//Bitmask 1110
			break;
		case 'D':
		case 'd':
			temp_array.push_back(bumper + 0);
			temp_array.push_back(bumper + 1);
			//temp_array.push_back(bumper + 2);
			temp_array.push_back(bumper + 3);
			//Bitmask 1101
			break;
		case 'C':
		case 'c':
			temp_array.push_back(bumper + 0);
			temp_array.push_back(bumper + 1);
			//temp_array.push_back(bumper + 2);
			//temp_array.push_back(bumper + 3);
			//Bitmask 1100
			break;
		case 'B':
		case 'b':
			temp_array.push_back(bumper + 0);
			//temp_array.push_back(bumper + 1);
			temp_array.push_back(bumper + 2);
			temp_array.push_back(bumper + 3);
			//Bitmask 1011
			break;
		case 'A':
		case 'a':
			temp_array.push_back(bumper + 0);
			//temp_array.push_back(bumper + 1);
			temp_array.push_back(bumper + 2);
			//temp_array.push_back(bumper + 3);
			//Bitmask 1010
			break;
		case '9':
			temp_array.push_back(bumper + 0);
			//temp_array.push_back(bumper + 1);
			//temp_array.push_back(bumper + 2);
			temp_array.push_back(bumper + 3);
			//Bitmask 1001
			break;
		case '8':
			temp_array.push_back(bumper + 0);
			//temp_array.push_back(bumper + 1);
			//temp_array.push_back(bumper + 2);
			//temp_array.push_back(bumper + 3);
			//Bitmask 1000
			break;
		case '7':
			//temp_array.push_back(bumper + 0);
			temp_array.push_back(bumper + 1);
			temp_array.push_back(bumper + 2);
			temp_array.push_back(bumper + 3);
			//Bitmask 0111
			break;
		case '6':
			//temp_array.push_back(bumper + 0);
			temp_array.push_back(bumper + 1);
			temp_array.push_back(bumper + 2);
			//temp_array.push_back(bumper + 3);
			//Bitmask 0110
			break;
		case '5':
			//temp_array.push_back(bumper + 0);
			temp_array.push_back(bumper + 1);
			//temp_array.push_back(bumper + 2);
			temp_array.push_back(bumper + 3);
			//Bitmask 0101
			break;
		case '4':
			//temp_array.push_back(bumper + 0);
			temp_array.push_back(bumper + 1);
			//temp_array.push_back(bumper + 2);
			//temp_array.push_back(bumper + 3);
			//Bitmask 0100
			break;
		case '3':
			//temp_array.push_back(bumper + 0);
			//temp_array.push_back(bumper + 1);
			temp_array.push_back(bumper + 2);
			temp_array.push_back(bumper + 3);
			//Bitmask 0011
			break;
		case '2':
			//temp_array.push_back(bumper + 0);
			//temp_array.push_back(bumper + 1);
			temp_array.push_back(bumper + 2);
			//temp_array.push_back(bumper + 3);
			//Bitmask 0010
			break;
		case '1':
			//temp_array.push_back(bumper + 0);
			//temp_array.push_back(bumper + 1);
			//temp_array.push_back(bumper + 2);
			temp_array.push_back(bumper + 3);
			//Bitmask 0001
			break;
		case '0':
			break;
		default:
			throw NIARL_TPM_ModuleV2::ERROR_ARG_VALIDATION;
		}
		bumper += 4;
	}
	
	size = temp_array.size();
	index = new int[size];

	for(int i = (size - 1); i > -1; i--)
	{
		index[i] = temp_array.back();
		temp_array.pop_back();
	}
}

NIARL_Util_Mask::~NIARL_Util_Mask()
{
	delete [] index;
}
